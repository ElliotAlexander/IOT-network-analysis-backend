package soton.gdp31.threads;

import soton.gdp31.database.DBScanHandler;
import soton.gdp31.database.DBTorHandler;
import soton.gdp31.utils.TorExitNodes.TorChecker;

import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBDeviceHandler;
import soton.gdp31.database.DBLocationHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.logger.Logging;
import soton.gdp31.manager.DeviceListManager;
import soton.gdp31.utils.GeoIpLocation.LocationFinder;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.utils.ScanProcessingQueue;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;
import soton.gdp31.cache.GeoLocationCache;
import soton.gdp31.utils.GeoIpLocation.GeoLocation;
import soton.gdp31.rating.RatingsManager;

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
    private soton.gdp31.threads.DeviceMonitorThread deviceMonitorThread;
    private GeoLocationCache geoLocationCache;
    private DBLocationHandler location_database_handler;
    private DBTorHandler tor_database_handler;
    private LocationFinder locationFinder;
    private TorChecker torChecker;

    private DBScanHandler scan_database_handler;

    private RatingsManager ratingsManager;

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
            this.scan_database_handler = new DBScanHandler(connection_handler);
            this.location_database_handler = new DBLocationHandler(connection_handler);
            this.tor_database_handler = new DBTorHandler(connection_handler);
            this.deviceMonitorThread = new soton.gdp31.threads.DeviceMonitorThread(deviceListManager, device_database_handler);
            this.torChecker = new TorChecker();
            this.ratingsManager = new RatingsManager(connection_handler, deviceListManager);
            return true;
        } catch (DBConnectionClosedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void run() {
        ratingsManager.start();

        while (true) {

            PacketWrapper p = PacketProcessingQueue.instance.packetQueue.poll();
            //Logging.logInfoMessage("PPT Size " + PacketProcessingQueue.instance.packetQueue.size());

            if(p == null || !p.isProcessable()){
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            device_database_handler.addToDatabase(p);

            // Get ports.
            int pSrcPort = p.getSrcPort();
            int pDestPort = p.getDestPort();

            if (deviceListManager.checkDevice(p.getUUID())) {
                // Each device has it's own internal representation - a device wrapper.
                // Get the correct device wrapper for the source / destination of this packet.
                DeviceWrapper deviceWrapper = deviceListManager.getDevice(p.getUUID());

                if(deviceWrapper.getIp() == null){
                    deviceWrapper.setIp(p.getAssociatedIpAddress());
                    Logging.logInfoMessage("Associated " + p.getAssociatedHostname() + " with ip " + p.getAssociatedIpAddress());
                } else if(!p.getAssociatedIpAddress().equals(deviceWrapper.getIp())){
                    device_database_handler.upateDeviceIp(p.getUUID(), p.getAssociatedIpAddress());
                }

                // Update internal representation.
                if (p.isHTTPS()) {
                    deviceWrapper.setHttpsPacketCount(deviceWrapper.getHttpsPacketCount() + 1);
                }

                deviceWrapper.setPacketCount(deviceWrapper.getPacketCount() + 1);
                deviceWrapper.setDataTransferred(deviceWrapper.getDataTransferred() + p.getPacketSize());
                deviceWrapper.addPortTraffic(p.getUniquePorts());

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
                        } else {
                            Logging.logWarnMessage("Failed to locate error for " + location_address);
                        }
                    } else { // Device has contacted it before in the Cache.
                            // Do nothing.
                    }

                    if(torChecker.checkNodeList(p.getLocation_address())){
                        tor_database_handler.addTorNode(p.getUUID(), p.getLocation_address(), true);
                    }
                }

                // Add packets to ArrayList for traffic.
                deviceWrapper.addPortTraffic(pSrcPort);
                deviceWrapper.addPortTraffic(pDestPort);

                if (p.getIsDNSPacket())
                    p.getDNSQueries().stream().forEach(query -> {
                        deviceWrapper.addDNSQuery(query);
                    });


                // Every ten seconds - update the database.
                if (System.currentTimeMillis() - deviceWrapper.getLastUpdateTime() > 3000) {
                    deviceWrapper.setLastUpdateTime(System.currentTimeMillis());
                    device_database_handler.updateLastSeen(deviceWrapper);
                    device_database_handler.updatePacketCounts(deviceWrapper, System.currentTimeMillis());
                    device_database_handler.updateDNSQueries(deviceWrapper);
                    geoLocationCache.pushCacheToDatabase();
                }


                if(System.currentTimeMillis() - deviceWrapper.getLastLiveUpdateTime() > 60000 ){
                    long current = System.currentTimeMillis();
                    device_database_handler.updateLivePacketCounts(deviceWrapper, current);
                    deviceWrapper.setLastLiveUpdateTime(current);
                }
            } else {
                // If we haven't seen a device before.
                System.out.println("Found new device " + p.getUUID() + " with ip " + p.getAssociatedIpAddress() + ". Hostname: " + p.getAssociatedHostname());
                DeviceWrapper device = deviceListManager.addDevice(p.getUUID(), p.getAssociatedIpAddress());

                device.addPortTraffic(pSrcPort);
                device.addPortTraffic(pDestPort);
                device_database_handler.updatePacketCounts(device, System.currentTimeMillis());

                // Add to scan ports queue.
                ScanProcessingQueue.instance.scanQueue.add(p);
            }
        }
    }

}