package soton.gdp31.wrappers;

import org.pcap4j.packet.DnsQuestion;
import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.database.DBUknownDeviceException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @Author ElliotAlexander
 *
 * This class represents the *internal representation* of a networked device.
 * Elements of this class may map onto the Database through DBDeviceHandler
 * This class may also be instantiated from the database on first boot.
 */
public class DeviceWrapper {

    private byte[] uuid;

    private int packet_count = 0;
    private int https_packet_count = 0;
    private List<DnsQuestion> dns_queries;

    private long last_update_time = -1;

    public DeviceWrapper(byte[] uuid){
        this.uuid = uuid;
        this.dns_queries = new ArrayList<>();
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

    public List<DnsQuestion> getDNSQueries() {
        return dns_queries;
    }

    public void addDNSQuery(DnsQuestion question) {
        dns_queries.add(question);
    }
    public void clearDNSQueries(){
        dns_queries.clear();
    }
}
