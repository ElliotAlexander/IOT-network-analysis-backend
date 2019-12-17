package cache;

import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import soton.gdp31.cache.DeviceUpdateCache;

public class UpdateCacheTest {

    private DeviceUpdateCache cache;

    @BeforeEach
    public void setup(){
        this.cache = new DeviceUpdateCache();
    }
}
