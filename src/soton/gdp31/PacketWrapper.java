package soton.gdp31;


import org.pcap4j.packet.IpPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.TcpPacket;
import org.pcap4j.packet.UdpPacket;

enum ProtocolType {
    TCP,
    UDP,
    UNKNOWN
};

public class PacketWrapper {

    private boolean isIpPacket;
    private int srcPort, destPort;

    private String srcIp;
    private String destIp;

    private boolean isHTTPS;
    private int packetSize;

    private long timestamp;
    private long packet_count;

    private ProtocolType protocol_type;

    public PacketWrapper(Packet p, long timestamp, long packet_count) {

        this.timestamp = timestamp;
        this.packet_count = packet_count;

        this.isIpPacket = p.contains(IpPacket.class);
        IpPacket ipPacket = p.get(IpPacket.class);
        this.srcIp = ipPacket.getHeader().getSrcAddr().getHostAddress().toString();
        this.destIp = ipPacket.getHeader().getDstAddr().getHostAddress().toString();

        String proto= ipPacket.getHeader().getProtocol().name();
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

    public ProtocolType getProtocol_type() {
        return protocol_type;
    }

    public long getPacket_count() {
        return packet_count;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
