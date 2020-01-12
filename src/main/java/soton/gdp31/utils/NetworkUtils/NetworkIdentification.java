package soton.gdp31.utils.NetworkUtils;

import org.apache.cassandra.dht.StringToken;
import soton.gdp31.logger.Logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.StringTokenizer;

public class NetworkIdentification {

    public static byte[] getSystemIp(){
        try {
            InetAddress localhost = InetAddress.getLocalHost();
            return localhost.getAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return new byte[]{};
        }
    }

    public static byte[] getNetworkMask(){
            try{
                InetAddress localHost = Inet4Address.getLocalHost();
                NetworkInterface networkInterface = NetworkInterface.getByInetAddress(localHost);
                int shift = 0xffffffff<<(32 - networkInterface.getInterfaceAddresses().get(0).getNetworkPrefixLength());
                int oct1 = ((byte) ((shift&0xff000000)>>24)) & 0xff;
                int oct2 = ((byte) ((shift&0x00ff0000)>>16)) & 0xff;
                int oct3 = ((byte) ((shift&0x0000ff00)>>8)) & 0xff;
                int oct4 = ((byte) (shift&0x000000ff)) & 0xff;
                String mask = oct1+"."+oct2+"."+oct3+"."+oct4;
                InetAddress maskIP = InetAddress.getByName(mask);
                return maskIP.getAddress();
            }catch(UnknownHostException e){
                System.out.println("Error: "+ e);
                return new byte[]{};
            } catch (SocketException e) {
                e.printStackTrace();
                return new byte[]{};
            }
    }

    public static byte[] getGatewayIP(){
        try {
            Process result = Runtime.getRuntime().exec("netstat -rn");
            BufferedReader output = new BufferedReader(new InputStreamReader(result.getInputStream()));
            Optional<String> gateway = output.lines().filter(s -> s.startsWith("default")).map(s -> {
                StringTokenizer st = new StringTokenizer(s);
                st.nextToken();
                return st.nextToken();
            }).findFirst();

            if (gateway.isPresent()) {
                InetAddress gatewayAddr = InetAddress.getByName(gateway.get());
                return gatewayAddr.getAddress();
            } else {
                return new byte[]{};
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[]{};
        }
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
}
