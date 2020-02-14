package soton.gdp31.utils;

import soton.gdp31.wrappers.PacketWrapper;

import java.util.concurrent.ConcurrentLinkedDeque;

public class PacketProcessingQueue {

    public static final PacketProcessingQueue instance = new PacketProcessingQueue();

    public ConcurrentLinkedDeque<PacketWrapper> packetQueue;

    private PacketProcessingQueue(){
        packetQueue = new ConcurrentLinkedDeque<>();
    }
}
