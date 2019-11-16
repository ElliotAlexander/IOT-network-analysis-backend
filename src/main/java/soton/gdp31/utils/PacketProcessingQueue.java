package soton.gdp31.utils;

import soton.gdp31.wrappers.PacketWrapper;

import java.util.concurrent.ConcurrentLinkedDeque;

public class PacketProcessingQueue {

    public static PacketProcessingQueue instance = new PacketProcessingQueue();

    ConcurrentLinkedDeque<PacketWrapper> concurrentLinkedQueue;


    private PacketProcessingQueue(){
        concurrentLinkedQueue = new ConcurrentLinkedDeque<>();
    }


    public void push(PacketWrapper p){
        concurrentLinkedQueue.push(p);
    }

    public PacketWrapper pop(){
        return concurrentLinkedQueue.pop();
    }

    public boolean isEmpty(){
        return concurrentLinkedQueue.isEmpty();
    }
}
