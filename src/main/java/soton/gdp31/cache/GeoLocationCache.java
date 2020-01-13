package soton.gdp31.cache;

import org.apache.commons.collections.map.HashedMap;
import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.GeoIpLocation.*;

public class GeoLocationCache {

    // Database connection.
    private DBConnection database_connection_handler;
    private Connection connection;

    // Destination address cache. IP address -> Last seen epoch time.

    // UUID ->
    HashMap<byte[], HashedMap<String, GeoLocation>> address_location_cache;

    // Cache timeout in milliseconds.
    public static final int CACHE_TIMEOUT = 10000;

    private static boolean instantiated;
    private static GeoLocationCache address_location_cache_instance;

    public static GeoLocationCache GeoLocationCacheInstance(DBConnection c){
        if(!instantiated){
            try {
                address_location_cache_instance = new GeoLocationCache(c);
            } catch (DBConnectionClosedException e){
                Logging.logErrorMessage("Failed to instantiate Device Object Cache instance. Exiting.");
                e.printStackTrace();
            }
            instantiated = true;
        }
        return device_object_cache_instance;
    }

    private GeoLocationCache(DBConnection database_connection_handler) throws DBConnectionClosedException {
        address_location_cache = new HashMap<byte[], HashMap<String, GeoLocation>>();
        this.database_connection_handler = database_connection_handler;
        this.connection = database_connection_handler.getConnection();
    }

    // TODO: Create pruning cache thread. Checks if device is still on last_seen deviceUUIDcache, if no, then kick off GeoLocationCache.
    // TODO: GeoLocationCache assumes that all devices are on DeviceUUIDCache.
    // TODO: Devices are added to GeoLocationCache when also added to the DeviceUUIDCache.
    // Check if a device exists in a database
    public boolean check_device_exists_has_ip_address(byte[] uuid, String ip_address){
        if(address_location_cache.containsKey(uuid)){
            list_of_locations = address_location_cache.get(uuid);// returns <IP Address, Location Object> hashmap

            if(list_of_locations.containsKey(ip_address)){
                if(list_of_locations.get(ip_address).last_scanned < (System.currentTimeMillis() - CACHE_TIMEOUT)){
                    list_of_locations.remove(ip_address);
                }
                return true;
            } else {
                // Has this device been scanned before?
                String query = "SELECT ip_address FROM ip_address_location WHERE ip_address = ?";
                try {
                    PreparedStatement ps = database_connection_handler.getConnection().prepareStatement(query);
                    ps.setBytes(1, ip_address);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next() == false){
                        return false;
                    } else {
                        return true;
                    }
                } catch (SQLException e) {
                    new DBExceptionHandler(e, database_connection_handler);
                    return false;
                } catch (DBConnectionClosedException e) {
                    new DBExceptionHandler(e, database_connection_handler);
                    return false;
                }
            }
        }
    }

    public boolean addIPAddress(byte[] uuid, String ip_address, GeoLocation location){
        try {
            if(address_location_cache.containsKey(uuid)){
                list_of_locations = address_location_cache.get(uuid); // Get list of ip_address -> locations

                if(list_of_locations.containsKey(ip_address)){
                    // Device has seen this IP address before.
                    GeoLocation location_object = list_of_locations.get(ip_address); // GeoLocation of ip_address in Cache.
                    last_scanned = location_object.last_scanned; // Last time GeoLocation was scanned.
                    if(last_scanned < (System.currentTimeMillis() - CACHE_TIMEOUT)){
                        // TODO: Remove location if it was scanned ages ago?
                    }
                    // Replace old cache of ip address with new one.
                    // TODO: Maybe a chokepoint, if you refreshing here.????
                    //GeoLocation refreshed_location = LookUpService.lookup(ip_address)
                    list_of_locations.replace(ip_address, location);
                    return true;
                } else {
                    // Device hasn't seen this IP address before.
                    list_of_locations.put(ip_address, location);
                }
            } else {
                // Device isn't in Cache. TODO: Pruner should mean that this almost never happens?
            }
        } catch (NoSuchAlgorithmException e){
            e.printStackTrace();
            return false;
        }
    }
}
