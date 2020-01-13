package soton.gdp31.cache;

import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBExceptionHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.UUIDGenerator;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DeviceUUIDCache {

    /**
     * DeviceObjectCache
     * @Author ElliotAlexander
     *
     * This class acts as a caching layer for checking whether a particular mac address has been seen before.
     * We need to identify new devices as they come in, but running a query per packet to check if a device exists
     * is a bad idea.
     *
     * Hence  - it makes sense to cache this information in memory.
     *
     * Device uuids are cached here, if a device isn't found in the cache we check the database.
     *
     * If this check returns false we can add a device to the database. We can also manually add a device to the cache.
     *
     */
    // Database connection wrapper
    private DBConnection database_connection_handler;
    private Connection connection;

    // Device UUID cache. UUID -> last seen epoch time.
    HashMap<byte[], Long> device_seen_cache;

    // Cache timeout in milliseconds
    public static final int CACHE_TIMEOUT = 10000;

    private static boolean instantiated;
    private static DeviceUUIDCache device_object_cache_instance;

    public static DeviceUUIDCache DeviceObjectCacheInstance(DBConnection c){
        if(!instantiated){
            try {
                device_object_cache_instance =  new DeviceUUIDCache(c);
            } catch (DBConnectionClosedException e) {
                Logging.logErrorMessage("Failed to instantiate Device Object Cache instance. Exiting.");
                e.printStackTrace();
            }
            instantiated = true;
        }
        return device_object_cache_instance;
    }

    private DeviceUUIDCache(DBConnection database_connection_handler) throws DBConnectionClosedException {
        device_seen_cache = new HashMap<byte[], Long>();
        this.database_connection_handler = database_connection_handler;
        this.connection = database_connection_handler.getConnection();
    }

    // Check if a device exists in the databsae.
    // @param byte[] uuid - The UUID of the device (a hash of the mac address).
    // @param Connection c - An open connection object.
    public boolean checkDeviceExists(byte[] uuid){
            if(device_seen_cache.containsKey(uuid)){
                if(device_seen_cache.get(uuid) < (System.currentTimeMillis() - CACHE_TIMEOUT)){
                    device_seen_cache.remove(uuid);
                }
                return true;
            } else {
                // Does the device exist?
                String query = "SELECT uuid FROM backend.devices WHERE uuid = ? UNION SELECT uuid FROM backend.device_stats WHERE uuid = ?";
                try {
                    PreparedStatement ps = database_connection_handler.getConnection().prepareStatement(query);
                    ps.setBytes(1, uuid);
                    ps.setBytes(2, uuid);
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

    public boolean addDevice(String mac_address){
        try {
            byte[] uuid = UUIDGenerator.generateUUID(mac_address);
            if(device_seen_cache.containsKey(uuid)){
                device_seen_cache.replace(uuid, System.currentTimeMillis());
                return true;
            } else {
                device_seen_cache.put(uuid, System.currentTimeMillis());
                return true;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
        }
    }
}
