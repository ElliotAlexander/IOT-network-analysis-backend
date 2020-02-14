package soton.gdp31.cache;


import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

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
        for(byte[] key : device_last_seen_cache.keySet()){
            if(Arrays.equals(uuid, key)){
                return true;
            }
        }
        return false;
    }

    public long getDevice(byte[] uuid){
        for(byte[] key: device_last_seen_cache.keySet()){
            if(Arrays.equals(key, uuid)){
                return device_last_seen_cache.get(key);
            }
        }
        return 0;
    }

    public boolean addDevice(byte[] uuid, long timestamp){
        device_last_seen_cache.put(uuid, timestamp);
        return true;
    }

    public void updateDevice(byte[] uuid, long timestamp){
        device_last_seen_cache.put(uuid, timestamp);
    }
}
