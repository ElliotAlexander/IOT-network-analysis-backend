package soton.gdp31.cache;


import soton.gdp31.database.DBConnection;

import java.sql.Connection;
import java.util.HashMap;

/**
 * @Author Elliot Alexander
 * This class acts as a caching layer for device timestamp updates
 * This may potentially affect several tables / fields in the database where regular time dependent data is needed.
 */
public class DeviceUpdateCache {

    private HashMap<byte[], Long> device_last_seen_cache;

    public DeviceUpdateCache(){
        device_last_seen_cache = new HashMap<>();
    }

    public boolean checkDevice(byte[] uuid) {
        return device_last_seen_cache.containsKey(uuid);
    }

    public long getDevice(byte[] uuid){
        return device_last_seen_cache.get(uuid);
    }

    public boolean addDevice(byte[] uuid, long timestamp){
        device_last_seen_cache.put(uuid, timestamp);
        return true;
    }
}
