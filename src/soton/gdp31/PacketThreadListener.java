package soton.gdp31;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapDumper;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.Packet;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

public class PacketThreadListener extends Thread {

    private final PcapHandle handle;
    private final PcapDumper pcap_dumper;



    public PacketThreadListener(PcapHandle handle, PcapDumper pcap_dumper) {
        this.handle = handle;
        this.pcap_dumper = pcap_dumper;
    }

    @Override
    public void run() {
        while (handle.isOpen()) {
            Packet radiotap_top_level_packet = null;

            try {
                // Get the next packet from the interface.
                // These are buffered, so we shouldn't miss any.
                radiotap_top_level_packet = handle.getNextPacketEx();
                pcap_dumper.dump(radiotap_top_level_packet);
            } catch (PcapNativeException | TimeoutException | NotOpenException | EOFException e) {
                System.out.println("Error - failed to maintain handle.");
                handle.close();
            }
        }
    }
}

