package soton.gdp31.utils.GeoIpLocation;

import com.google.common.net.InetAddresses;
import soton.gdp31.utils.NetworkUtils.HostnameFetcher;

public class LookUpService {

    public static GeoLocation lookup(String ipAddress) throws UnknownHostException{

        try {
            File database = new File("soton/gdp31/utils/GeoIpLocation/GeoLite2-City.mmdb");
            DatabaseReader reader = new DatabaseReader.Builder(database).build();

            InetAddresses addr = InetAddresses.getByName(ipAddress);
            String hostname = addr.getHostName();
            String last_scanned = System.currentTimeMillis();
            return GeoLocation.map(reader.city(adr).getLocation(), hostname, last_scanned);

        } catch (IOException e) {
            System.out.println("Could not load geo ip database: " + e.getMessage());
        }
    }
}
