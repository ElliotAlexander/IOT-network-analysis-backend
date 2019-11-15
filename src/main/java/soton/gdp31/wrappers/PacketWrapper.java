package soton.gdp31.wrappers;


import org.apache.cassandra.utils.UUIDGen;
import org.pcap4j.packet.*;
import soton.gdp31.enums.ProtocolType;
import soton.gdp31.exceptions.InvalidIPPacketException;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.UUIDGenerator;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;

public class PacketWrapper {

    private boolean isIpPacket;
    private int srcPort, destPort;

    private String hostname;

    private String srcIp;
    private String destIp;

    private boolean isHTTPS;
    private int packetSize;

    private long timestamp;
    private long packet_count;

    private String src_mac_address;
    private String dest_mac_address;

    private ProtocolType protocol_type;

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
        this.srcIp = ipPacket.getHeader().getSrcAddr().getHostAddress();
        this.destIp = ipPacket.getHeader().getDstAddr().getHostAddress();

        try {
            this.uuid = UUIDGenerator.generateUUID(src_mac_address);
            if(this.uuid == null){
                Logging.logErrorMessage("Failed to generate UUID for packet.");
                throw new NoSuchAlgorithmException();
            }
        } catch(NoSuchAlgorithmException e) {
            Logging.logErrorMessage("Error initialising connections for device " + src_mac_address);
            e.printStackTrace();
        }

        try {
            InetAddress host = InetAddress.getByName(this.srcIp);
            this.hostname = host.getHostName();
        } catch (UnknownHostException e) {
            Logging.logWarnMessage("Error resolving hostname for " + this.srcIp);
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
        return srcIp;
    }

    public String getDestIp() {
        return destIp;
    }

    public boolean isHTTPS() {
        return isHTTPS;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public String getHostname(){
        return this.hostname;
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
}
