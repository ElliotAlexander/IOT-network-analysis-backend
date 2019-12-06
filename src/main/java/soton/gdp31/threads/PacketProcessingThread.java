package soton.gdp31.threads;

import soton.gdp31.cache.DeviceUpdateCache;
import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBPacketHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.devices.UnknownDeviceException;
import soton.gdp31.manager.DeviceListManager;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;

public class PacketProcessingThread extends Thread {

    private DBConnection connection_handler;
    private DBPacketHandler packet_handler;
    private DeviceUpdateCache timestamp_update_cache;
    private DeviceListManager deviceListManager;

    public PacketProcessingThread() {
        try {
            this.connection_handler = new DBConnection();
            this.deviceListManager = new DeviceListManager(connection_handler);
            this.packet_handler = new DBPacketHandler(connection_handler);
            this.timestamp_update_cache = new DeviceUpdateCache();
        } catch (DBConnectionClosedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {

            if(!PacketProcessingQueue.instance.isEmpty()){

                long timestamp = System.currentTimeMillis();

                PacketWrapper p = PacketProcessingQueue.instance.pop();
                if(deviceListManager.checkDevice(p.getUUID())){
                    try {
                        deviceListManager.updateStats(p);
                        DeviceWrapper deviceWrapper = deviceListManager.getDevice(p.getUUID());
                        if(timestamp_update_cache.checkDevice(p.getUUID())){
                            if(timestamp_update_cache.getDevice(p.getUUID()) < timestamp - 10000){
                               packet_handler.updateDeviceStats(deviceWrapper, timestamp);
                            }
                        } else {
                            packet_handler.updateDeviceStats(deviceWrapper, timestamp);
                            timestamp_update_cache.addDevice(p.getUUID(), timestamp);
                        }
                        packet_handler.commitPacketToDatabase(p);
                    } catch (UnknownDeviceException e) {
                        e.printStackTrace();
                    }
                } else {
                    DeviceWrapper device = deviceListManager.addDevice(p.getUUID());
                    packet_handler.commitPacketToDatabase(p);
                    packet_handler.updateDeviceStats(device, timestamp);
                    timestamp_update_cache.addDevice(p.getUUID(), timestamp);

                }
            }
        }
    }
}
