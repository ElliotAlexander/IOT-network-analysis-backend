package soton.gdp31.threads;

import soton.gdp31.cache.DeviceUpdateCache;
import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBPacketHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.manager.DeviceListManager;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;

public class PacketProcessingThread extends Thread {

    private DBConnection connection_handler;
    private DBPacketHandler packet_handler;
    private DeviceListManager deviceListManager;

    public PacketProcessingThread() {
        try {
            this.connection_handler = new DBConnection();
            this.deviceListManager = new DeviceListManager(connection_handler);
            this.packet_handler = new DBPacketHandler(connection_handler);
        } catch (DBConnectionClosedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {

            if(!PacketProcessingQueue.instance.isEmpty()){
                PacketWrapper p = PacketProcessingQueue.instance.pop();
                packet_handler.commitPacketToDatabase(p);

                if(deviceListManager.checkDevice(p.getUUID())) {
                    DeviceWrapper deviceWrapper = deviceListManager.getDevice(p.getUUID());
                    if (p.isHTTPS()) {
                        deviceWrapper.setHttpsPacketCount(deviceWrapper.getHttpsPacketCount() + 1);
                    }


                    deviceWrapper.setPacketCount(deviceWrapper.getPacketCount() + 1);
                    if (System.currentTimeMillis() - deviceWrapper.getLastUpdateTime() > 10000) {
                        System.out.println("Updating device " + p.getUUID());
                        deviceWrapper.setLastUpdateTime(System.currentTimeMillis());
                        packet_handler.updateDeviceTimestamp(p);
                        packet_handler.updateDeviceStats(deviceWrapper, System.currentTimeMillis());
                    }
                } else{
                    System.out.println("Found new device " + p.getUUID());
                        DeviceWrapper device = deviceListManager.addDevice(p.getUUID());
                        packet_handler.commitPacketToDatabase(p);
                        packet_handler.updateDeviceStats(device, System.currentTimeMillis());
                    }
                }
        }
    }
}