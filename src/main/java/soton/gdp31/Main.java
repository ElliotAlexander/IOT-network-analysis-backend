package soton.gdp31;

import org.pcap4j.core.*;

import soton.gdp31.exceptions.InterfaceUnknownException;
import soton.gdp31.logger.Logging;
import soton.gdp31.threads.PacketListenerThread;
import soton.gdp31.threads.PacketProcessingThread;
import soton.gdp31.utils.NetworkUtils.InterfaceUtils;
import soton.gdp31.utils.NetworkUtils.NetworkIdentification;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    public static byte[] SYSTEM_IP;
    public static byte[] GATEWAY_IP;
    public static byte[] SUBNET_MASK;

    public static final String interface_name = "en0";
    public static final String handle_dump_name = "out.pcap";



    public static void main(String[] args) {
        new Main();
    }

    public Main() {

        printInterfaces();

        // Setup basic network information.
        this.SYSTEM_IP = NetworkIdentification.getSystemIp();
        this.SUBNET_MASK = NetworkIdentification.getNetworkMask();
        this.GATEWAY_IP = NetworkIdentification.getGatewayIP();
        try {
            Logging.logInfoMessage("Gateway IP address: " + InetAddress.getByAddress(this.GATEWAY_IP));
            Logging.logInfoMessage("Network Mask: " + InetAddress.getByAddress(this.SUBNET_MASK));
            Logging.logInfoMessage("System IP: " + InetAddress.getByAddress(this.SYSTEM_IP));
        } catch (UnknownHostException e) {
            Logging.logErrorMessage("Error fetching network information.");
            e.printStackTrace();
        }



        // Start our listening thread.
        Logging.logInfoMessage("Starting packet listner thread");
        PacketListenerThread plt = new PacketListenerThread();
        plt.start();

        Logging.logInfoMessage("Starting packet processing thread.");
        PacketProcessingThread ppt = new PacketProcessingThread();
        ppt.start();

        Thread threadPool[] = { ppt, plt };
        while(true){
            for(Thread t : threadPool){
                if(!t.isAlive() || t == null){
                    try {
                        Logging.logErrorMessage("Thread " + t.getName() + " crashed. Attempting to reinstantiate.");
                        Class<?> clazz = Class.forName(t.getClass().getName());
                        Constructor<?> ctor = clazz.getConstructor();
                        Object object = ctor.newInstance(new Object[] {});
                        Thread tNew = (Thread) object;
                        t = tNew;
                        t.start();
                        Logging.logInfoMessage("Successfully restarted thread.");
                    } catch (ClassNotFoundException | NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }

                }
            }
        }

    }

    public static void printInterfaces() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets))
                displayInterfaceInformation(netint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.printf("Display name: %s\n", netint.getDisplayName());
        System.out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("InetAddress: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }
}
