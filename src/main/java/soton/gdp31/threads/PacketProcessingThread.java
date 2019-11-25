package soton.gdp31.threads;

import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.packet.EthernetPacket;
import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBPacketHandler;
import soton.gdp31.exceptions.InvalidIPPacketException;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.utils.PacketProcessingQueue;
import soton.gdp31.wrappers.PacketWrapper;

import java.io.EOFException;
import java.sql.Connection;
import java.util.concurrent.TimeoutException;

public class PacketProcessingThread extends Thread {

    private DBConnection connection_handler;
    private DBPacketHandler packet_handler;

    public PacketProcessingThread() {
        try {
            this.connection_handler = new DBConnection();
            this.packet_handler = new DBPacketHandler(connection_handler);
        } catch (DBConnectionClosedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while(true) {
            if(!PacketProcessingQueue.instance.isEmpty()){
                PacketWrapper p = PacketProcessingQueue.instance.pop();
                packet_handler.commitPacketToDatabase(p);
            }
        }
    }
}
