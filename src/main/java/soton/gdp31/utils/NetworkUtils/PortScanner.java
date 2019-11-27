import java.io.IOException;
import java.net.*;


public class PortScanner {

    public static int port;

    private PortScanner(String hostname){
        try {
            startThreads(hostname);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void startThreads(String hostname) throws InterruptedException {
        int numThreads = 50;
        // Total ports: 65,535 TCP Ports and 65,535 UDP Ports
        // 65,535 ports divded by 50 threads = 1,310.7 ports per thread
        int portsPerThread = 1311;

        Threads[] threads = new Thread[numThreads];

        System.out.println("Creating threads to portscan...")
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(new PortRunner(hostname, portsPerThread));
            threads[i].start();
        }

        for (int i = 0; i < threads.length; i++) {
            threads[i].join();
        }

        System.out.println("Threads created...");
    }
}