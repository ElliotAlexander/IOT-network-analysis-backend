package soton.gdp31.utils.IP2Location;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.lang.Throwable;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Subdivision;

public class GeoIPLocationService {
    private DatabaseReader dbReader;

    public GeoIPLocationService() throws IOException {
        File database = new File("GeoLite2-City.mmdb");
        dbReader = new DatabaseReader.Builder(database).build();
    }

    public GeoIP getLocation(String ip) {
        InetAddress ipAddress = InetAddress.getByName(ip);

        try {
            CityResponse response = dbReader.city(ipAddress);

            Country country = response.getCountry();
            City city = response.getCity();
            Location location = response.getLocation();

            GeoIP geoIP = new GeoIP(country, city, location);
        } catch (GeoIp2Exception e) {
            e.printStackTrace();
        }


    }
}