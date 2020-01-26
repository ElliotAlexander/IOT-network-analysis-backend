package soton.gdp31.threads;

import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBDeviceHandler;
import soton.gdp31.database.DBLocationHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.logger.Logging;
import soton.gdp31.manager.DeviceListManager;
import soton.gdp31.utils.GeoIpLocation.LocationFinder;
import soton.gdp31.utils.NetworkUtils.HostnameFetcher;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;
import soton.gdp31.cache.GeoLocationCache;
import soton.gdp31.utils.GeoIpLocation.GeoLocation;
import soton.gdp31.utils.GeoIpLocation.LocationFinder;

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

    private GeoLocationCache geoLocationCache;
    private DBLocationHandler location_database_handler;
    private LocationFinder locationFinder;

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
            this.locationFinder = new LocationFinder();
            this.connection_handler = new DBConnection();
            this.geoLocationCache = GeoLocationCache.GeoLocationCacheInstance(connection_handler);
            this.deviceListManager = new DeviceListManager(connection_handler);
            this.device_database_handler = new DBDeviceHandler(connection_handler);

            this.location_database_handler = new DBLocationHandler(connection_handler);
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

                // Location provider.
                if(p.isLocationable()){
                    // Set device UUID.
                    byte[] device_uuid = p.getUUID();

                    // Get external destination/source address.
                    String location_address = p.getLocation_address();

                    // Has this address been located before, within a list.
                    Boolean address_has_located = geoLocationCache.needsLocating(device_uuid, location_address);

                    if(address_has_located){
                        // Look up the address.
                        GeoLocation location = locationFinder.lookup(location_address);
                        // Store the location in the cache.
                        if(location != null) {
                            geoLocationCache.storeLocation(device_uuid, location_address, location);
                        }
                    } else { // Device has contacted it before in the Cache.
                            // Do nothing.
                    }

                }


                if (p.getIsDNSPacket())
                    p.getDNSQueries().stream().forEach(query -> {
                        deviceWrapper.addDNSQuery(query);
                    });


                // Every ten seconds - update the database.
                if (System.currentTimeMillis() - deviceWrapper.getLastUpdateTime() > 10000) {
                    deviceWrapper.setLastUpdateTime(System.currentTimeMillis());
                    device_database_handler.updateLastSeen(deviceWrapper);
                    device_database_handler.updatePacketCounts(deviceWrapper, System.currentTimeMillis());
                    device_database_handler.updateDNSQueries(deviceWrapper);
                    geoLocationCache.pushCacheToDatabase();
                }
            } else {
                // If we haven't seen a device before.
                System.out.println("Found new device " + p.getUUID());
                DeviceWrapper device = deviceListManager.addDevice(p.getUUID());
                System.out.println("Hostname: " + HostnameFetcher.fetchHostname(p.getSrcIp()));
                device_database_handler.updatePacketCounts(device, System.currentTimeMillis());
            }

        }
    }
}