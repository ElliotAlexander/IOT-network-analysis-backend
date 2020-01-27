package soton.gdp31.threads;

import org.omg.SendingContext.RunTime;
import org.pcap4j.core.*;
import org.pcap4j.packet.*;
import soton.gdp31.Main;
import soton.gdp31.exceptions.InterfaceUnknownException;
import soton.gdp31.exceptions.InvalidIPPacketException;
import soton.gdp31.exceptions.devices.IPv6DeviceException;
import soton.gdp31.exceptions.network.UnhandledTrafficException;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.NetworkUtils.InterfaceUtils;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.PacketWrapper;

import java.io.EOFException;
import java.util.concurrent.TimeoutException;

public class PacketListenerThread extends Thread {

    private PcapHandle handle;

    private long packet_count = 0;



    public PacketListenerThread() {
        // Setup PCAP interface, file dump, database connection and monitoring thread.
        try {
            this.handle = InterfaceUtils.openInterface(Main.interface_name);
        } catch (InterfaceUnknownException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public PacketListenerThread(long packet_count) {
        this.packet_count = packet_count;
    }

    @Override
    public void run() {
        Logging.logInfoMessage("Started packet listener thread.");
        while (handle.isOpen()) {
            EthernetPacket ethernet_packet = null;
            try {
                ethernet_packet = (EthernetPacket) handle.getNextPacketEx();
                packet_count++;
                PacketWrapper packet = new PacketWrapper(ethernet_packet, handle.getTimestamp().toInstant().toEpochMilli(), packet_count);
                PacketProcessingQueue.instance.packetQueue.push(packet);
            } catch (PcapNativeException | TimeoutException | NotOpenException | EOFException e) {
                Logging.logErrorMessage("Error - failed to maintain handle.");
                handle.close();
                Logging.logErrorMessage("Attempting to restore handle.");
                try {
                    this.handle = InterfaceUtils.openInterface(Main.interface_name);
                } catch (InterfaceUnknownException e1){
                    Logging.logErrorMessage("Interface " + Main.interface_name + " is no longer available. The packet listener thread will exit, and attempt to rebuild itself.");
                    throw new RuntimeException();
                }
            } catch (InvalidIPPacketException e){
                continue;
            } catch (ClassCastException e){
                e.printStackTrace();
                Logging.logErrorMessage("Skipping non-ethernet packet");
            } catch (UnhandledTrafficException e) {
                continue;
            } catch (IPv6DeviceException e) {
                Logging.logInfoMessage("Skipping IPv6 device.");
            }
        }
    }
}

