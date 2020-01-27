package soton.gdp31;


import soton.gdp31.config.ConfigLoader;
import soton.gdp31.exceptions.network.InvalidInterfaceAddressException;
import soton.gdp31.logger.Logging;
import soton.gdp31.threads.PacketListenerThread;
import soton.gdp31.threads.PacketProcessingThread;
import soton.gdp31.utils.NetworkUtils.NetworkIdentification;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.UnknownHostException;

public class Main {


    public static final String config_name = "config.txt";
    public static byte[] SYSTEM_IP;
    public static byte[] GATEWAY_IP;
    public static byte[] SUBNET_MASK;
    public static byte[] BROADCAST_SUBNET_ADDRESS;

    // This is a list of the configuration options we'll declare below, and load from the config file.
    private static final String configOptions[] = {
            "interface_name",
            "pcap_dump_file",
            "pcap_dump_enabled",
            "logfile_output",
            "logfile_enabled",
            "hardcode_system_ip",
            "hardcode_subnet_mask",
            "hardcode_gateway_ip",
    };

    // Loadable configuration options.
    public static String interface_name;
    public static String pcap_dump_file;
    public static boolean pcap_dump_enabled;
    public static String logfile_output;
    public static boolean logfile_enabled;

    public static String hardcode_system_ip;
    public static String hardcode_subnet_mask;
    public static String hardcode_gateway_ip;

    public static final int PPT_THREAD_COUNT = 1;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {

        // load our config file
        Properties prop = ConfigLoader.instance.loadConfig("config.txt");
        setupConfigurationOptions(prop);
        printInterfaces();

        // Setup basic network information.

        try {
            this.SYSTEM_IP = hardcode_system_ip == null ? NetworkIdentification.getSystemIp() : InetAddress.getByName(hardcode_system_ip).getAddress();
            this.SUBNET_MASK = hardcode_subnet_mask == null ? NetworkIdentification.getNetworkMask() :  InetAddress.getByName(hardcode_subnet_mask).getAddress();
            this.GATEWAY_IP = hardcode_gateway_ip == null ? NetworkIdentification.getGatewayIP() :  InetAddress.getByName(hardcode_gateway_ip).getAddress();
            this.BROADCAST_SUBNET_ADDRESS = NetworkIdentification.getMaxIpValue(this.GATEWAY_IP, this.SUBNET_MASK);
            Logging.logInfoMessage("Gateway IP address: " + InetAddress.getByAddress(this.GATEWAY_IP));
            Logging.logInfoMessage("Network Mask: " + InetAddress.getByAddress(this.SUBNET_MASK));
            Logging.logInfoMessage("System IP: " + InetAddress.getByAddress(this.SYSTEM_IP));
            Logging.logInfoMessage("Broadcast Subnet Address: " + InetAddress.getByAddress(this.BROADCAST_SUBNET_ADDRESS));
        } catch (UnknownHostException e) {
            Logging.logErrorMessage("Error fetching network information.");
            e.printStackTrace();
        } catch (InvalidInterfaceAddressException e) {
            Logging.logErrorMessage("Failed to parse network mask.");
            e.printStackTrace();
        }

        // Instantiate

        // Start our listening thread.
        Logging.logInfoMessage("Starting packet listner thread");
        PacketListenerThread plt = new PacketListenerThread();
        plt.start();

        ArrayList<Thread> threadPool = new ArrayList<Thread>();
        threadPool.add(plt);

        for(int i = 0; i < PPT_THREAD_COUNT; i++){
            Logging.logInfoMessage("Starting packet processing thread number " + i);
            PacketProcessingThread ppt = new PacketProcessingThread();
            ppt.start();
            threadPool.add(ppt);
        }

        while(true){
            for(Thread t : threadPool){
                if(!t.isAlive() || t == null){
                    try {
                        Logging.logErrorMessage("Thread " + t.getName() + " crashed. Attempting to reinstantiate.");
                        Class<?> clazz = Class.forName(t.getClass().getName());
                        Constructor<?> ctor = clazz.getConstructor();
                        Object object = ctor.newInstance(new Object[] {});
                        Thread tNew = (Thread) object;
                        tNew.start();
                        threadPool.remove(t);
                        threadPool.add(tNew);
                        Logging.logInfoMessage("Successfully restarted thread.");
                        continue;
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
        System.out.printf("[INFO] Display name: %s\n", netint.getDisplayName());
        System.out.printf("[INFO] Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("[INFO] InetAddress: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }

    private void setupConfigurationOptions(Properties props){

        for(String option: this.configOptions){
            try {
                if(this.getClass().getField(option).getType().isAssignableFrom(String.class)){
                    this.getClass().getField(option).set(this, props.getProperty(option));
                    Logging.logInfoMessage("Loaded String config option " + option + " with value " + this.getClass().getField(option).get(this));
                } else {
                    this.getClass().getField(option).set(this, props.getProperty(option) == "true" ? true : false);
                    Logging.logInfoMessage("Loaded boolean configuration option " + option + " with value " + this.getClass().getField(option).get(this));
                }
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
                Logging.logErrorMessage("Failed to load configuration option " + option + ". Invalid field name specified.");
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                Logging.logErrorMessage("Failed to load configuration option " + option + ". Invalid access modifier specified. ");
            }
        }
    }
}
