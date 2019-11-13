package soton.gdp31.utils;

import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.util.HashMap;

public class DeviceObjectCache {

    public static DeviceObjectCache device_object_cache = new DeviceObjectCache();

    HashMap<byte[], Long> device_seen_cache;

    // Cache timeout in seconds
    public static final int CACHE_TIMEOUT = 10000;

    private DeviceObjectCache(){
        device_seen_cache = new HashMap<byte[], Long>();
    }


    public boolean checkDeviceExists(String mac_address){
        try {
            byte[] uuid = UUIDGenerator.generateUUID(mac_address);
            if(device_seen_cache.containsKey(uuid)){
                if(device_seen_cache.get(uuid) < (System.currentTimeMillis() - CACHE_TIMEOUT)){
                    device_seen_cache.remove(uuid);
                    return false;
                } else {
                    return true;
                }
            } else {
                return false;
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return false;
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
