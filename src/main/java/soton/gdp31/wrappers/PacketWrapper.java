package soton.gdp31.wrappers;


import org.apache.cassandra.utils.UUIDGen;
import org.pcap4j.packet.*;
import soton.gdp31.Main;
import soton.gdp31.cache.DeviceHostnameCache;
import soton.gdp31.enums.ProtocolType;
import soton.gdp31.exceptions.InvalidIPPacketException;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.NetworkUtils.HostnameFetcher;
import soton.gdp31.utils.NetworkUtils.NetworkIdentification;
import soton.gdp31.utils.UUIDGenerator;

import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class PacketWrapper {

    private boolean isIpPacket;
    private int srcPort, destPort;

    private String src_hostname;
    private String dest_hostname;

    private boolean is_internal_traffic;

    private String src_ip;
    private String dest_ip;

    private boolean isHTTPS;
    private int packetSize;

    private long timestamp;
    private long packet_count;

    private String src_mac_address;
    private String dest_mac_address;

    private ProtocolType protocol_type;

    private boolean is_dns_packet;
    private List<DnsQuestion> dns_address;

    private boolean is_broadcast_traffic = false;
    private String associated_mac_address;
    private String associated_hostname;

    private byte[] uuid;

    public PacketWrapper(EthernetPacket p, long timestamp, long packet_count) throws InvalidIPPacketException {

        this.timestamp = timestamp;
        this.packet_count = packet_count;

        src_mac_address = p.getHeader().getSrcAddr().toString();
        dest_mac_address = p.getHeader().getDstAddr().toString();

        this.isIpPacket = p.contains(IpPacket.class);
        IpPacket ipPacket = p.get(IpPacket.class);

        if(ipPacket == null) {
            throw new InvalidIPPacketException();
        }
        this.src_ip = ipPacket.getHeader().getSrcAddr().getHostAddress();
        this.dest_ip = ipPacket.getHeader().getDstAddr().getHostAddress();

        if (ipPacket.contains(DnsPacket.class)) {
            DnsPacket.DnsHeader dnsPacketHeader = p.get(DnsPacket.class).getHeader();
            this.is_dns_packet = true;
            this.dns_address = dnsPacketHeader.getQuestions();
        }

        src_hostname = DeviceHostnameCache.instance.checkHostname(ipPacket.getHeader().getSrcAddr().getAddress(), is_internal_traffic);
        if( src_hostname == null){
            src_hostname = HostnameFetcher.fetchHostname(src_ip);
        }

        dest_hostname = DeviceHostnameCache.instance.checkHostname(ipPacket.getHeader().getDstAddr().getAddress(), is_internal_traffic);
        if( dest_hostname == null){
            dest_hostname = HostnameFetcher.fetchHostname(src_ip);
        }

        if(this.src_ip == "0.0.0.0" || this.dest_ip == "0.0.0.0"){
            this.is_broadcast_traffic = true;
            if(this.src_ip == "0.0.0.0" && this.dest_ip == "0.0.0.0" ){
                Logging.logErrorMessage("This is a weird packet?");
            }
            try {
                if (this.src_ip == "0.0.0.0") {
                    this.uuid = UUIDGenerator.generateUUID(src_mac_address);
                    associated_mac_address = src_mac_address;
                    associated_hostname = src_hostname;
                } else {
                    this.uuid = UUIDGenerator.generateUUID(dest_mac_address);
                    associated_mac_address = dest_mac_address;
                    associated_hostname = dest_hostname;
                }
            }catch(NoSuchAlgorithmException e) {
                Logging.logErrorMessage("Error initialising connections for device " + src_mac_address);
                e.printStackTrace();
            }
        } else {
            boolean src_is_internal = NetworkIdentification.compareIPSubnets(ipPacket.getHeader().getSrcAddr().getAddress(), Main.GATEWAY_IP, Main.SUBNET_MASK);
            boolean dest_is_internal = NetworkIdentification.compareIPSubnets(ipPacket.getHeader().getDstAddr().getAddress(), Main.GATEWAY_IP, Main.SUBNET_MASK);
            this.is_internal_traffic = (dest_is_internal && src_is_internal);
            try {
                if ( is_internal_traffic || src_is_internal) {      // Is the traffic purely internal? Or is just the source internal
                    this.uuid = UUIDGenerator.generateUUID(src_mac_address);
                    DeviceHostnameCache.instance.addDevice(src_hostname, uuid, true);
                    associated_mac_address = src_mac_address;
                    associated_hostname = src_hostname;
                } else if(dest_is_internal) {        // Destination is internal, source is external
                    this.uuid = UUIDGenerator.generateUUID(dest_mac_address);
                    DeviceHostnameCache.instance.addDevice(dest_hostname, uuid, false);
                    associated_mac_address = dest_mac_address;
                    associated_hostname = dest_hostname;
                } else {
                    this.uuid = UUIDGenerator.generateUUID(src_mac_address);
                    DeviceHostnameCache.instance.addDevice(src_hostname, uuid, true);
                    associated_mac_address = src_mac_address;
                    associated_hostname = src_hostname;;
                }

                if (this.uuid == null) {
                    Logging.logErrorMessage("Failed to generate UUID for packet.");
                    throw new NoSuchAlgorithmException();
                }
            } catch(NoSuchAlgorithmException e) {
                Logging.logErrorMessage("Error initialising connections for device " + src_mac_address);
                e.printStackTrace();
            }
        }


        String proto = ipPacket.getHeader().getProtocol().name();
        try {
                if (proto.equalsIgnoreCase("TCP")) {
                    TcpPacket tcpPkt = p.get(TcpPacket.class);
                    srcPort = tcpPkt.getHeader().getSrcPort().valueAsInt();
                    destPort = tcpPkt.getHeader().getDstPort().valueAsInt();
                    this.protocol_type = ProtocolType.TCP;
                } else if (proto.equalsIgnoreCase("UDP")) {
                    UdpPacket udpPkt = p.get(UdpPacket.class);
                    srcPort = udpPkt.getHeader().getSrcPort().valueAsInt();
                    destPort = udpPkt.getHeader().getDstPort().valueAsInt();
                    this.protocol_type = ProtocolType.UDP;
                } else {
                    this.protocol_type = ProtocolType.UNKNOWN;
                }
            } catch (Exception e) {
                return;
            }

        if (srcPort == 80 || srcPort == 8080 || destPort == 80 || destPort == 8080 ){
            this.isHTTPS = true;
        }else if(srcPort == 443 || destPort == 443 ){
            this.isHTTPS = false;
        }

            this.packetSize = p.length();
    }

    public boolean isIpPacket() {
        return isIpPacket;
    }

    public String getSrcIp() {
        return src_ip;
    }

    public String getDestIp() {
        return dest_ip;
    }

    public boolean isHTTPS() {
        return isHTTPS;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public String getSrcHostname(){
        return this.src_hostname;
    }

    public String getDestHostname(){
        return this.dest_hostname;
    }

    public byte[] getUUID(){
        return this.uuid;
    }

    public ProtocolType getProtocol_type() {
        return protocol_type;
    }

    public long getPacket_count() {
        return packet_count;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString(){
        return "[" + packet_count + "] " + Long.toString(timestamp) + " | " + (this.isHTTPS ? "HTTPS" : "HTTP") + ":" + this.protocol_type.toString() + " " + this.getSrcIp() + ":" + this.srcPort + " -> " + this.getDestIp() + ":" + this.destPort;
    }

    public int getSrcPort(){
        return srcPort;
    }

    public int getDestPort(){
        return destPort;
    }

    public boolean getIsDNSPacket() {
        return is_dns_packet;
    }

    public List<DnsQuestion> getDNSQueries(){
        return dns_address;
    }

    public String getAssociatedMacAddress() {
        return associated_mac_address;
    }

    public String getAssociatedHostname(){
        return associated_hostname;
    }

    public boolean isBroadcastTraffic() {
        return is_broadcast_traffic;
    }

    public String getSrcMacAddress(){
        return this.src_mac_address;
    }

    public String getDestMacAddress(){
        return this.dest_mac_address;
    }
}
