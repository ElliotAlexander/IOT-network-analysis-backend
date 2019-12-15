package soton.gdp31.threads;

import soton.gdp31.cache.DeviceUpdateCache;
import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBPacketHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.manager.DeviceListManager;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;

/**
 * @Author Elliot Alexander
 * <p>
 * Packet processing thread is responsible for pulling packet objects from the shared queue, and handling updating the
 * database / internal representation, before discarding the packet object.
 */
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
        while (true) {

            if (!PacketProcessingQueue.instance.isEmpty()) {
                PacketWrapper p = PacketProcessingQueue.instance.pop();
                packet_handler.addDeviceToDatabase(p);

                if (deviceListManager.checkDevice(p.getUUID())) {
                    // Each device has it's own internal representation - a device wrapper.
                    // Get the correct device wrapper for the source / destination of this packet.
                    DeviceWrapper deviceWrapper = deviceListManager.getDevice(p.getUUID());

                    // Update internal representation.
                    if (p.isHTTPS()) {
                        deviceWrapper.setHttpsPacketCount(deviceWrapper.getHttpsPacketCount() + 1);
                    }

                    deviceWrapper.setPacketCount(deviceWrapper.getPacketCount() + 1);


                    // Every ten seconds - update the database.
                    if (System.currentTimeMillis() - deviceWrapper.getLastUpdateTime() > 10000) {
                        deviceWrapper.setLastUpdateTime(System.currentTimeMillis());
                        packet_handler.updateDeviceTimestamp(p);
                        packet_handler.updateDeviceStats(deviceWrapper, System.currentTimeMillis());
                    }
                } else {
                    // If we haven't seen a device before.
                    System.out.println("Found new device " + p.getUUID());
                    DeviceWrapper device = deviceListManager.addDevice(p.getUUID());
                    packet_handler.updateDeviceStats(device, System.currentTimeMillis());
                }
            }
        }
    }
}