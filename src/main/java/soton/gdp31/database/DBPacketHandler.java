package soton.gdp31.database;

import com.sun.media.sound.SoftFilter;
import soton.gdp31.utils.DeviceObjectCache;
import soton.gdp31.wrappers.PacketWrapper;

import java.sql.Connection;



public class DBPacketHandler {
    public boolean commitPacketToDatabase(PacketWrapper p, Connection c) {
        // if(DeviceObjectCache.device_object_cache.checkDeviceExists())
        return true;
    }
}
