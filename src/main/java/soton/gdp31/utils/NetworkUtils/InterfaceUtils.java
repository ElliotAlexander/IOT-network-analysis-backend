package soton.gdp31.utils.NetworkUtils;

import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.Pcaps;
import org.pcap4j.util.NifSelector;
import soton.gdp31.Main;
import soton.gdp31.exceptions.InterfaceUnknownException;
import soton.gdp31.logger.Logging;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;

public class InterfaceUtils {

    public static void displayInterfaceInformation(NetworkInterface netint) throws SocketException {
        Logging.logInfoMessage("Display name: " + netint.getDisplayName());
        Logging.logInfoMessage("Name: " + netint.getName());
        Enumeration<InetAddress> inetAddresses = netint.getInetAddresses();
        for (InetAddress inetAddress : Collections.list(inetAddresses)) {
            Logging.logInfoMessage("InetAddress: " +  inetAddress);
        }
    }


    public static void getInterfaces() {
        try {
            Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
            for (NetworkInterface netint : Collections.list(nets))
                InterfaceUtils.displayInterfaceInformation(netint);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PcapHandle openInterface(String interface_name) throws InterfaceUnknownException{
        try {
            int snapLen = 65536;
            int timeout = 1000;

            PcapNetworkInterface nif = Pcaps.getDevByName(interface_name);

            if(nif == null){
                Logging.logInfoMessage("Failed to load from System IP. Attempting to load from display name.");
                Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
                for (NetworkInterface netint : Collections.list(nets))
                    if(netint.getName() == interface_name){
                        Logging.logInfoMessage("Attempting to load interface " + netint.getName());
                        nif = Pcaps.getDevByName(netint.getName());
                    }
            }

            PcapNetworkInterface.PromiscuousMode mode = PcapNetworkInterface.PromiscuousMode.PROMISCUOUS;
            Logging.logInfoMessage("Opening interface " + interface_name);
            PcapHandle handle = nif.openLive(snapLen, mode, timeout);
            Logging.logInfoMessage("Successfully opened interface on " + interface_name);
            return handle;
        } catch (PcapNativeException e) {
            e.printStackTrace();
            throw new InterfaceUnknownException(interface_name);
        } catch (Exception e) {
            Logging.logErrorMessage("Error - " + e.toString() + "\n ");
            e.printStackTrace();
            throw new InterfaceUnknownException(interface_name);
        }
    }

}
