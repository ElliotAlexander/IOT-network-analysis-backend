package soton.gdp31.utils.NetworkUtils;

import org.apache.cassandra.dht.StringToken;
import soton.gdp31.Main;
import soton.gdp31.exceptions.network.InvalidInterfaceAddressException;
import soton.gdp31.logger.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringTokenizer;

public class NetworkIdentification {

    public static byte[] getSystemIp(){
        try {
            NetworkInterface networkInterface = NetworkInterface.getByName(Main.interface_name);
            for(InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                if(address.getAddress() instanceof Inet4Address){
                    return address.getAddress().getAddress();
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return new byte[]{};
        }
        return new byte[]{};
    }

    public static long ipToLong(InetAddress ip) {
        byte[] octets = ip.getAddress();
        long result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
        }
        return result;
    }

    public static byte[] getNetworkMask() throws InvalidInterfaceAddressException {
            try{
                NetworkInterface networkInterface = NetworkInterface.getByName(Main.interface_name);
                for(InterfaceAddress address : networkInterface.getInterfaceAddresses()){
                    if(address.getAddress() instanceof Inet4Address){
                        Logging.logInfoMessage("Found correct address " + address.getAddress());
                        int shift = 0xffffffff<<(32 - networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength());
                        int oct1 = ((byte) ((shift&0xff000000)>>24)) & 0xff;
                        int oct2 = ((byte) ((shift&0x00ff0000)>>16)) & 0xff;
                        int oct3 = ((byte) ((shift&0x0000ff00)>>8)) & 0xff;
                        int oct4 = ((byte) (shift&0x000000ff)) & 0xff;
                        String mask = oct1+"."+oct2+"."+oct3+"."+oct4;
                        InetAddress maskIP = InetAddress.getByName(mask);
                        return maskIP.getAddress();
                    } else {
                        Logging.logInfoMessage("Added ipv6 address");
                        Logging.logWarnMessage("Failed to load interface with valid IPv4 address. Is your address ipv6 only?");
                        continue;
                    }
                }
                return null;

            }catch(UnknownHostException e){
                System.out.println("Error: "+ e);
                return new byte[]{};
            } catch (SocketException e) {
                e.printStackTrace();
                return new byte[]{};
            }
    }

    private static byte[] getGatewayFromNetstat(){
        try {
            Process result = Runtime.getRuntime().exec("netstat -rn");
            BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));

            if(System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
                Logging.logInfoMessage("Identified OS as Windows.");
                String line;
                while( (line = output.readLine()) != null) {
                    if(line.contains("Active Routes")){
                        String line_title = output.readLine();      // Active Routes:
                        String default_gateway = output.readLine(); // Network Destination        Netmask          Gateway       Interface  Metric
                        // Next line is the default gateway
                        Logging.logInfoMessage("Loaded default gateway line as: \n\t " + default_gateway);
                        ArrayList<String> output_columns = new ArrayList<>();
                        String built_string = "";
                        for(char s : default_gateway.toCharArray()){
                            if(s == ' '){
                                if(built_string != "") {
                                    output_columns.add(built_string);
                                    built_string = "";
                                }
                            } else {
                                built_string += s;
                            }
                        }

                        if(output_columns.size() > 3 && output_columns.get(2) != null){
                            Logging.logInfoMessage("Gateway: " + output_columns.get(2));
                            return InetAddress.getByName(output_columns.get(2)).getAddress();
                        } else {
                            Logging.logInfoMessage("Failed to load output from Netstat on Windows.");
                            return null;
                        }
                    }
                }
            } else {
                Logging.logInfoMessage("Idenfified OS as unix based.");
                Logging.logInfoMessage("Attempting to grab gateway IP from netstat.");
                Optional<String> gateway = output.lines().filter(s -> s.startsWith("default")).filter(s -> s.contains(Main.interface_name)).map(s -> {
                    StringTokenizer st = new StringTokenizer(s);
                    st.nextToken();
                    return st.nextToken();
                }).findFirst();

                if (gateway.isPresent()) {
                    Logging.logInfoMessage("Parsed gateway: " + gateway.get());
                    InetAddress gatewayAddr = InetAddress.getByName(gateway.get());
                    return gatewayAddr.getAddress();
                } else {
                    return null;
                }
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static byte[] getGatewayFromInternalRouting(){
        try(DatagramSocket s=new DatagramSocket())
        {
            s.connect(InetAddress.getByAddress(new byte[]{1,1,1,1}), 0);
            byte[] address =  NetworkInterface.getByName(Main.interface_name).getHardwareAddress();
            if(address == null){
                Logging.logErrorMessage("Failed to load gateway ip (Attempt 2).");
            }
            Logging.logInfoMessage("Attempting to pull gateway from router...");
            Logging.logInfoMessage("Address length " + address.length);
            Logging.logInfoMessage("Raw Ip to string: " + getIpAddress(address));
            Logging.logInfoMessage("Loaded gateway ip of " + InetAddress.getByAddress(address).toString());
            return address;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String getIpAddress(byte[] rawBytes) {
        int i = 4;
        StringBuilder ipAddress = new StringBuilder();
        for (byte raw : rawBytes) {
            ipAddress.append(raw & 0xFF);
            if (--i > 0) {
                ipAddress.append(".");
            }
        }
        return ipAddress.toString();
    }

    private static byte[] getGatewayInformationFromTraceroute(){
        Process result = null;
        try {
            result = Runtime.getRuntime().exec("traceroute -m 1 www.amazon.com");
            BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));
            String thisLine = output.readLine();
            StringTokenizer st = new StringTokenizer(thisLine);
            st.nextToken();
            String gateway = st.nextToken();
            if(gateway == null){
                return null;
            } else {
                System.out.printf("The gateway is %s\n", gateway);
                return InetAddress.getByName(gateway).getAddress();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getGatewayIP() throws InvalidInterfaceAddressException {
        Logging.logInfoMessage("Attempting to parse gateway info from Internal Routing");
        byte[] res = getGatewayFromInternalRouting();
        if(res == null){
            Logging.logInfoMessage("Attempting to parse gateway info from netstat.");
            res = getGatewayFromNetstat();
            if(res == null){
                res = getGatewayInformationFromTraceroute();
                if(res  == null){
                    throw new InvalidInterfaceAddressException("Failed to find local interface");
                }
            }
        }

        return res;
    }


    public static boolean compareIPSubnets(byte[] a1, byte[] a2, byte[] networkMask){
        try {
            for (int i = 0; i < a1.length; i++)
                if ((a1[i] & networkMask[i]) != (a2[i] & networkMask[i]))
                    return false;
        } catch (Exception e){
            return false;
        }

        return true;
    }

    public static byte[] getMaxIpValue(byte[] ip_addr, byte[] networkMask ){
        byte[] returnVal = new byte[ip_addr.length];
        byte[] returnVal2 = new byte[ip_addr.length];

        try {
            for (int i = 0; i < ip_addr.length; i++)
                returnVal[i] = new Integer(ip_addr[i] & networkMask[i]).byteValue();

            for (int i = 0; i < ip_addr.length; i++)
                returnVal2[i] = new Integer(ip_addr[i] | ~networkMask[i]).byteValue();
        } catch (Exception e){
            Logging.logErrorMessage("Error loading max ip subnet.");
            e.printStackTrace();
            return returnVal;
        }
        return returnVal2;

    }

    public static byte[] getSubnetRoot(byte[] ip_addr, byte[] networkMask ){
        byte[] returnVal = new byte[ip_addr.length];
        try {
            for (int i = 0; i < ip_addr.length; i++)
                returnVal[i] = new Integer(ip_addr[i] & networkMask[i]).byteValue();
        } catch (Exception e){
            Logging.logErrorMessage("Error loading max ip subnet.");
            e.printStackTrace();
            return returnVal;
        }
        return returnVal;

    }
}
