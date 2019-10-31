package soton.gdp31;

import org.pcap4j.core.*;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class Main {

    private final String interface_name = "en0";
    private final String handle_dump_name = "out.pcap";

    private PcapHandle handle;
    private PcapDumper dumper;

    private final PacketThreadListener thread;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        printInterfaces();
        openPcapInterfaces();
        this.thread = new PacketThreadListener(handle, dumper);
        this.thread.start();
    }


    public void printInterfaces() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets))
                displayInterfaceInformation(netint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        System.out.printf("Display name: %s\n", netint.getDisplayName());
        System.out.printf("Name: %s\n", netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            System.out.printf("InetAddress: %s\n", inetAddress);
        }
        System.out.printf("\n");
    }

    private PcapHandle openInterfaces(String interface_name) throws RuntimeException {
        int snapLen = 65536;
        int timeout = 1000;

        try {
            PcapNetworkInterface nif = Pcaps.getDevByName(interface_name);
            PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
            System.out.println("Opening interface " + interface_name);
            PcapHandle handle = nif.openLive(snapLen, mode, timeout);
            System.out.println("Successfully opened interface on " + interface_name);
            return handle;
        } catch (PcapNativeException e) {
            throw new RuntimeException();
        }
    }

    private void openPcapInterfaces(){
        try {
            handle = openInterfaces(this.interface_name);
            dumper = handle.dumpOpen(this.handle_dump_name);
        } catch (Exception e) {
            System.out.println("Error - " + e.toString() + "\n ");
            e.printStackTrace();
        }
    }

}
