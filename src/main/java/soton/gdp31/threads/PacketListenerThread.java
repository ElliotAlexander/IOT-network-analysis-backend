package soton.gdp31.threads;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import soton.gdp31.database.DBPacketHandler;
import soton.gdp31.exceptions.InvalidIPPacketException;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.PacketWrapper;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

public class PacketListenerThread extends Thread {

    private final PcapHandle handle;
    private final PcapDumper pcap_dumper;

    private long packet_count = 0;

    private DBPacketHandler packetHandler = new DBPacketHandler();


    public PacketListenerThread(PcapHandle handle, PcapDumper pcap_dumper) {
        this.handle = handle;
        this.pcap_dumper = pcap_dumper;
    }

    public PacketListenerThread(PcapHandle handle, PcapDumper pcap_dumper, long packet_count) {
        this(handle, pcap_dumper);
        this.packet_count = packet_count;
    }

    @Override
    public void run() {
        while (handle.isOpen()) {
            EthernetPacket ethernet_packet = null;
            try {
                ethernet_packet = (EthernetPacket) handle.getNextPacketEx();
                packet_count++;
                PacketWrapper packet = new PacketWrapper(ethernet_packet, handle.getTimestamp().toInstant().toEpochMilli(), packet_count);
                PacketProcessingQueue.instance.push(packet);
                pcap_dumper.dump(ethernet_packet);
            } catch (PcapNativeException | TimeoutException | NotOpenException | EOFException e) {
                System.out.println("Error - failed to maintain handle.");
                handle.close();
            } catch (InvalidIPPacketException e){
                continue;
            }
        }
    }
}

