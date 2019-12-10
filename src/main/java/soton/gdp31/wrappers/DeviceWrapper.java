package soton.gdp31.wrappers;

import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.database.DBUknownDeviceException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DeviceWrapper {

    private Connection c;
    private byte[] uuid;

    private int packet_count = 0;
    private int https_packet_count = 0;

    private long last_update_time = -1;

    public DeviceWrapper(byte[] uuid){
        this.uuid = uuid;
    }

    public byte[] getUUID() {
        return uuid;
    }

    public int getHttpsPacketCount() {
        return https_packet_count;
    }

    public void setHttpsPacketCount(int https_packet_count_new) {
        this.https_packet_count = https_packet_count_new;
    }

    public int getPacketCount() {
        return packet_count;
    }

    public void setPacketCount(int packet_count_new) {
        this.packet_count = packet_count_new;
    }

    public long getLastUpdateTime() {
        return last_update_time;
    }

    public void setLastUpdateTime(long last_update_time) {
        this.last_update_time = last_update_time;
    }
}
