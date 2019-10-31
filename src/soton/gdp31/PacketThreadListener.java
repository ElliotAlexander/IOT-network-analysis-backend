package soton.gdp31;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

public class PacketThreadListener extends Thread {

    private final PcapHandle handle;
    private final PcapDumper pcap_dumper;

    private long packet_count = 0;

    public PacketThreadListener(PcapHandle handle, PcapDumper pcap_dumper) {
        this.handle = handle;
        this.pcap_dumper = pcap_dumper;
    }

    public PacketThreadListener(PcapHandle handle, PcapDumper pcap_dumper, long packet_count) {
        this(handle, pcap_dumper);
        this.packet_count = packet_count;
    }

    @Override
    public void run() {
        while (handle.isOpen()) {
            Packet radiotap_top_level_packet = null;
            try {
                radiotap_top_level_packet = handle.getNextPacketEx();
                packet_count++;
                PacketWrapper packet = new PacketWrapper(radiotap_top_level_packet, handle.getTimestamp().toInstant().toEpochMilli(), packet_count);
            } catch (PcapNativeException | TimeoutException | NotOpenException | EOFException e) {
                System.out.println("Error - failed to maintain handle.");
                handle.close();
            }
        }
    }
}

