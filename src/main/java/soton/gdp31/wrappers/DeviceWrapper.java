package soton.gdp31.wrappers;

import org.pcap4j.packet.DnsQuestion;
import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.database.DBUknownDeviceException;

import java.net.InetAddress;
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

    private long packet_count = 0;
    private long https_packet_count = 0;
    private long data_transferred = 0;
    private long data_in = 0;
    private long data_out = 0;
    private InetAddress ip;

    private List<DnsQuestion> dns_queries;

    private long last_update_time = -1;

    public DeviceWrapper(byte[] uuid, InetAddress ip){
        this.uuid = uuid;
        this.ip = ip;
        this.dns_queries = new ArrayList<>();
    }

    public DeviceWrapper(byte[] uuid){
        this.uuid = uuid;
        this.dns_queries = new ArrayList<>();
    }

    public byte[] getUUID() {
        return uuid;
    }

    public InetAddress getIp(){
        return this.ip;
    }

    public void setIp(InetAddress ip){
        this.ip = ip;
    }

    public long getHttpsPacketCount() {
        return https_packet_count;
    }

    public void setHttpsPacketCount(long https_packet_count_new) {
        this.https_packet_count = https_packet_count_new;
    }

    public long getPacketCount() {
        return packet_count;
    }

    public void setPacketCount(long packet_count_new) {
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

    public void setDataTransferred(long new_data_transferred){
        this.data_transferred = new_data_transferred;
    }

    public long getDataTransferred(){
        return this.data_transferred;
    }


    public long getDataIn() {
        return data_in;
    }

    public void setDataIn(long data_in) {
        this.data_in = data_in;
    }

    public long getDataOut() {
        return data_out;
    }

    public void setDataOut(long data_out) {
        this.data_out = data_out;
    }
}
