
import org.junit.jupiter.api.*;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.Pcaps;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import soton.gdp31.Main;
import soton.gdp31.utils.NetworkUtils.NetworkIdentification;
import soton.gdp31.wrappers.PacketWrapper;
import soton.gdp31.enums.ProtocolType;

import static org.junit.Assert.*;

/**
 * @test InterfaceTest
 * @author elliotalexander
 *
 * This test case initialisaes an example ICMP packet capture, and validates that objects are created correctly from this
 * example packet.
 *
 */
class InterfaceTest {
    /*

    private PcapHandle ph;



    @BeforeEach
    public void setupPcapHandle() throws Exception {
        ph = Pcaps.openOffline("src/test/resources/example.pcap");
        Main.SYSTEM_IP = NetworkIdentification.getSystemIp();
        Main.SUBNET_MASK = NetworkIdentification.getNetworkMask();
        Main.GATEWAY_IP = NetworkIdentification.getGatewayIP();
    }


    @Test
    public void validateInitialPacket() throws Exception {
        assertNotNull(ph);
        EthernetPacket p = (EthernetPacket) ph.getNextPacket();
        assertNotNull(p);

        long currentTimeMills = System.currentTimeMillis();
        PacketWrapper pw = new PacketWrapper(p, currentTimeMills, 0);


        // Getters and setters initialise properly.
        assertEquals(pw.getTimestamp(), currentTimeMills);
        assertEquals(pw.getPacket_count(), 0);


        assertEquals(pw.getDestIp(), "2.1.1.1");
        assertEquals(pw.getSrcIp(), "2.1.1.2");
        assertEquals(pw.getProtocol_type(), ProtocolType.UNKNOWN);
    }


     */
}
