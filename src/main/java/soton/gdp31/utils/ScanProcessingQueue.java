package soton.gdp31.utils;

import soton.gdp31.wrappers.PacketWrapper;

import java.util.concurrent.ConcurrentLinkedDeque;

public class ScanProcessingQueue {

    public static final ScanProcessingQueue instance = new ScanProcessingQueue();

    public ConcurrentLinkedDeque<PacketWrapper> scanQueue;

    private ScanProcessingQueue(){
        scanQueue = new ConcurrentLinkedDeque<>();
    }
}
