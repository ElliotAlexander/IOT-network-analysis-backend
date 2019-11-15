package soton.gdp31.utils;

import soton.gdp31.logger.Logging;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class DeviceObjectCache {

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

    public static DeviceObjectCache device_object_cache = new DeviceObjectCache();

    HashMap<byte[], Long> device_seen_cache;

    // Cache timeout in seconds
    public static final int CACHE_TIMEOUT = 10000;

    private DeviceObjectCache(){
        device_seen_cache = new HashMap<byte[], Long>();
    }

    // Check if a device exists in the databsae.
    // @param byte[] uuid - The UUID of the device (a hash of the mac address).
    // @param Connection c - An open connection object.
    public boolean checkDeviceExists(byte[] uuid, Connection c){
            if(device_seen_cache.containsKey(uuid)){
                if(device_seen_cache.get(uuid) < (System.currentTimeMillis() - CACHE_TIMEOUT)){
                    device_seen_cache.remove(uuid);
                }
                return true;
            } else {
                // Does the device exist?
                String query = "SELECT uuid FROM devices WHERE uuid = ? UNION SELECT uuid FROM device_stats WHERE uuid = ?";
                try {
                    PreparedStatement ps = c.prepareStatement(query);
                    ps.setBytes(1, uuid);
                    ps.setBytes(2, uuid);
                    ResultSet rs = ps.executeQuery();
                    if(rs.next() == false){
                        return false;
                    } else {
                        return true;
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
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
