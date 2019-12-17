package cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import soton.gdp31.cache.DeviceHostnameCache;

import java.util.Random;

public class HostnameCacheTest {

    private DeviceHostnameCache cache;
    private byte[] randomUUID = new byte[64];

    @BeforeEach
    public void beforeSetup(){
        this.cache = DeviceHostnameCache.instance;
        Random random = new Random();
        random.nextBytes(randomUUID);;
    }


    @Test
    public void addTest(){
        cache.addDevice("NewDevice", randomUUID, false );
        assert(cache.checkHostname(randomUUID, false) != "");
        assert(cache.checkHostname(randomUUID, true) == "");
    }
}
