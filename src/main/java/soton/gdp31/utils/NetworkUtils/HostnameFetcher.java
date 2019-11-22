package soton.gdp31.utils.NetworkUtils;

import soton.gdp31.logger.Logging;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class HostnameFetcher {

    public static String fetchHostname(String ip){
        try {
            InetAddress host = InetAddress.getByName(ip);
            return host.getHostName();
        } catch (UnknownHostException e) {
            Logging.logWarnMessage("Error resolving hostname for " + ip);
            return "";
        }
    }
}
