package soton.gdp31.cache;

import java.util.HashMap;

public class DeviceHostnameCache {

    HashMap<byte[], String> internal_hostname_cache;
    HashMap<byte[], String> external_hostname_cache;
    public static final int CACHE_TIMEOUT = 10000;

    public static final DeviceHostnameCache instance = new DeviceHostnameCache();

    private DeviceHostnameCache(){
        internal_hostname_cache = new HashMap<>();
        external_hostname_cache = new HashMap<>();
    }

    public String checkHostname(byte[] uuid, boolean is_internal){
        if(is_internal) {
            if(internal_hostname_cache.containsKey(uuid)){
                return internal_hostname_cache.get(uuid);
            } else {
                return null;
            }
        } else {
            if(external_hostname_cache.containsKey(uuid)){
                return external_hostname_cache.get(uuid);
            } else {
                return null;
            }
        }
    }

    public void addDevice(String hostname, byte[] uuid, boolean is_internal){
        if(is_internal){
            internal_hostname_cache.put(uuid, hostname);
        } else {
            external_hostname_cache.put(uuid, hostname);
        }
    }
}
