import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;
import soton.gdp31.exceptions.InvalidIPPacketException;
import soton.gdp31.wrappers.PacketWrapper;

public class PacketWrapperTest {

    private PacketWrapper wrapper;

    @BeforeEach
    public void setupWrapperObject(){
        try {
            PcapHandle ph = Pcaps.openOffline("src/test/resources/SSL-Single.pcapng");
            this.wrapper = new PacketWrapper((EthernetPacket) ph.getNextPacket(), System.currentTimeMillis(), 1);
        } catch (PcapNativeException e) {
            e.printStackTrace();
        } catch (NotOpenException e) {
            e.printStackTrace();
        } catch (InvalidIPPacketException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void httpsIdentificationTest(){
        assert this.wrapper.isHTTPS() == false;
    }

    @Test
    public void subnetIdentificationTest(){
        assert this.wrapper.isOutgoingTraffic() == false;
    }

    @Test
    public void assertPortSolicitation(){
        assert this.wrapper.getDestPort() == 56711;
        assert this.wrapper.getSrcPort() == 4433;
    }


    @Test
    public void assertIpSolicitation() {
        assert wrapper.getSrcIp() == "127.0.0.1";
        assert wrapper.getDestIp() == "127.0.0.1";

    }
}
