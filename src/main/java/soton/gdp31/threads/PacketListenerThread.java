package soton.gdp31.threads;

import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import soton.gdp31.Main;
import soton.gdp31.database.DBPacketHandler;
import soton.gdp31.exceptions.InterfaceUnknownException;
import soton.gdp31.exceptions.InvalidIPPacketException;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.NetworkUtils.InterfaceUtils;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.PacketWrapper;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

public class PacketListenerThread extends Thread {

    private PcapHandle handle;
    private PcapDumper pcap_dumper;

    private long packet_count = 0;



    public PacketListenerThread() {
        // Setup PCAP interface, file dump, database connection and monitoring thread.
        try {
            this.handle = InterfaceUtils.openInterface(Main.interface_name);
            this.pcap_dumper = handle.dumpOpen(Main.handle_dump_name);
        } catch (PcapNativeException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (InterfaceUnknownException e) {
            e.printStackTrace();
            System.exit(1);
        } catch (NotOpenException e) {
            Logging.logErrorMessage("Failed to open handle.");
            e.printStackTrace();
        }
    }

    public PacketListenerThread(long packet_count) {
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
            } catch (PcapNativeException | TimeoutException | NotOpenException | EOFException e) {
                System.out.println("Error - failed to maintain handle.");
                handle.close();
            } catch (InvalidIPPacketException e){
                continue;
            }
        }
    }
}

