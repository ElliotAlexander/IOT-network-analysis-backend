package soton.gdp31.wrappers;

import main.java.soton.gdp31.utils.PortScanning.PortScanResult;
import org.pcap4j.packet.DnsQuestion;
import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.database.DBUknownDeviceException;

import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
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
    private long last_rating_time = -1;

    private ArrayList<Integer> port_traffic;

    public DeviceWrapper(byte[] uuid, InetAddress ip){
        this.uuid = uuid;
        this.ip = ip;
        this.dns_queries = new ArrayList<>();
        this.port_traffic = new ArrayList<>();
    }

    public DeviceWrapper(byte[] uuid){
        this.uuid = uuid;
        this.dns_queries = new ArrayList<>();
        this.port_traffic = new ArrayList<>();

    }

    public String getPortTrafficString(){
            ArrayList<Integer> results = getPort_traffic();
            Iterator i = results.iterator();
            ArrayList<Integer> list_of_ints = new ArrayList<>();
            while(i.hasNext()){
                PortScanResult scanResult = (PortScanResult) i.next();
                int portNumber = scanResult.getPort();
                if(scanResult.isOpen()) {
                    list_of_ints.add(portNumber);
                }
            }

            StringBuilder sb = new StringBuilder();
            for(int j = list_of_ints.size() - 1; j >= 0; j--){
                int num = list_of_ints.get(j);
                sb.append(num);
                sb.append(',');
            }

            // Remove last ','
            String result = sb.toString();
            result = result.substring(0, result.length()-1);

            return result;
    }

    public void addPortTraffic(int port){
        if(!port_traffic.contains(port)){
            port_traffic.add(port);
        }
    }
    public ArrayList<Integer> getPort_traffic() {
        return port_traffic;
    }

    public void setPort_traffic(ArrayList<Integer> port_traffic) {
        this.port_traffic = port_traffic;
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

    public long getLast_rating_time() {
        return last_rating_time;
    }

    public void setLast_rating_time(long last_rating_time) {
        this.last_rating_time = last_rating_time;
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
