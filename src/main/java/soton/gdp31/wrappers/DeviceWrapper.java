package soton.gdp31.wrappers;

import soton.gdp31.logger.Logging;
import org.pcap4j.packet.DnsQuestion;


import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javafx.util.Pair;

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
    private long last_live_update_time = -1;

    private HashMap<Integer, Integer> port_traffic;

    public DeviceWrapper(byte[] uuid, InetAddress ip){
        this.uuid = uuid;
        this.ip = ip;
        this.dns_queries = new ArrayList<>();
        this.port_traffic = new HashMap<>();
    }

    public DeviceWrapper(byte[] uuid){
        this.uuid = uuid;
        this.dns_queries = new ArrayList<>();
        this.port_traffic = new HashMap<>();

    }

    public String getPortTrafficString(){
            HashMap<Integer,Integer> results = getPortTraffic();
            StringBuilder sb  = new StringBuilder();
            for(Integer i : results.keySet()){
                sb.append(i + ":" + results.get(i) + ",");
            }
            String result = sb.toString();
            return result.substring(0, result.length()-1);
    }

    public void addPortTraffic(int port){
        if(!port_traffic.keySet().contains(port)){
            port_traffic.put(port, 1);
        } else {
            port_traffic.replace(port, port_traffic.get(port) + 1);
        }
    }

    public void addPortTraffic(int[] ports){
        for(int i : ports){
            addPortTraffic(i);
        }
    }

    public void setPortTraffic(int port, int count){
        if(!port_traffic.keySet().contains(port)){
            port_traffic.put(port, count);
        }
    }

    public HashMap<Integer, Integer> getPortTraffic() {
        return port_traffic;
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

    public long getLastLiveUpdateTime() {
        return last_live_update_time;
    }

    public void setLastLiveUpdateTime(long last_live_update_time) {
        this.last_live_update_time = last_live_update_time;
    }
}
