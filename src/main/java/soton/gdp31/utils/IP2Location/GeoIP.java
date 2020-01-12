package soton.gdp31.utils.IP2Location;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.Subdivision;

public class GeoIP {
    private String ipAddress;
    private Country country;
    private City city;
    private Location location;


    // constructors, getters and setters

    public GeoIP(Country country, City city, Location location) {
        this.ipAddress = ipAddress;
        this.city = city;
        this.location = location;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setCity(City city) {
        this.city = city;
    }

    public City getCity() {
        return city;
    }
    
    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation(){
        return location;
    }

}