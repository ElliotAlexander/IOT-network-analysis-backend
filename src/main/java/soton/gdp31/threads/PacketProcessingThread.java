package soton.gdp31.threads;

import main.java.soton.gdp31.database.DBScanHandler;
import main.java.soton.gdp31.utils.PortScanning.PortScanResult;
import main.java.soton.gdp31.utils.PortScanning.PortScanner;
import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBDeviceHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.logger.Logging;
import soton.gdp31.manager.DeviceListManager;
import soton.gdp31.utils.NetworkUtils.HostnameFetcher;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;

import java.util.ArrayList;

/**
 * @Author Elliot Alexander
 * <p>
 * Packet processing thread is responsible for pulling packet objects from the shared queue, and handling updating the
 * database / internal representation, before discarding the packet object.
 */
public class PacketProcessingThread extends Thread {

    private DBConnection connection_handler;
    private DBDeviceHandler device_database_handler;
    private DeviceListManager deviceListManager;

    private DBScanHandler scan_database_handler;

    public PacketProcessingThread() {
        while(openConnections() == false){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean openConnections(){
        try {
            this.connection_handler = new DBConnection();
            this.deviceListManager = new DeviceListManager(connection_handler);
            this.device_database_handler = new DBDeviceHandler(connection_handler);

            this.scan_database_handler = new DBScanHandler(connection_handler);
            return true;
        } catch (DBConnectionClosedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        while (true) {

            PacketWrapper p = PacketProcessingQueue.instance.packetQueue.poll();

            if(p == null){
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            device_database_handler.addToDatabase(p);

            if (deviceListManager.checkDevice(p.getUUID())) {
                // Each device has it's own internal representation - a device wrapper.
                // Get the correct device wrapper for the source / destination of this packet.
                DeviceWrapper deviceWrapper = deviceListManager.getDevice(p.getUUID());

                // Update internal representation.
                if (p.isHTTPS()) {
                    deviceWrapper.setHttpsPacketCount(deviceWrapper.getHttpsPacketCount() + 1);
                }

                deviceWrapper.setPacketCount(deviceWrapper.getPacketCount() + 1);
                deviceWrapper.setDataTransferred(deviceWrapper.getDataTransferred() + p.getPacketSize());
                if(p.getAssociatedMacAddress() ==  p.getDestMacAddress()){
                    deviceWrapper.setDataIn(deviceWrapper.getDataIn() + p.getPacketSize());
                } else {
                    deviceWrapper.setDataOut(deviceWrapper.getDataOut() + p.getPacketSize());
                }


                if (p.getIsDNSPacket())
                    p.getDNSQueries().stream().forEach(query -> deviceWrapper.addDNSQuery(query));


                // Every ten seconds - update the database.
                if (System.currentTimeMillis() - deviceWrapper.getLastUpdateTime() > 10000) {
                    deviceWrapper.setLastUpdateTime(System.currentTimeMillis());
                    device_database_handler.updateLastSeen(deviceWrapper);
                    device_database_handler.updatePacketCounts(deviceWrapper, System.currentTimeMillis());
                    device_database_handler.updateDNSQueries(deviceWrapper);
                }
            } else {
                // If we haven't seen a device before.
                System.out.println("Found new device " + p.getUUID());
                DeviceWrapper device = deviceListManager.addDevice(p.getUUID());
                System.out.println("Hostname: " + HostnameFetcher.fetchHostname(p.getSrcIp()));
                device_database_handler.updatePacketCounts(device, System.currentTimeMillis());

                // Scan ports initially.
                scanPorts(p, true);
            }

        }
    }

    public void scanPorts(PacketWrapper p, boolean initial){
        PortScanner scanner = new PortScanner();
        String device_ip_address = p.getSrcIp();

        ArrayList<PortScanResult> results = scanner.scan_device_ports(device_ip_address);

        String string_for_db = scanner.extract_list_of_open_ports(results);

        if(initial){
            scan_database_handler.addToDatabase(p.getUUID(), string_for_db);
        } else {
            scan_database_handler.updateDatabase(p.getUUID(), string_for_db);
        }
    }
}