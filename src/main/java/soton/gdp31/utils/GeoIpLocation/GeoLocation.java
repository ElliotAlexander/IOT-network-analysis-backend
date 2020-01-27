package soton.gdp31.utils.GeoIpLocation;

import com.maxmind.db.CHMCache;
import com.maxmind.db.NoCache;
import com.maxmind.db.NodeCache;
import com.maxmind.db.Reader.FileMode;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.AddressNotFoundException;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.*;
import com.maxmind.geoip2.model.*;
import com.maxmind.geoip2.record.Country;
import com.maxmind.geoip2.record.City;
import com.maxmind.geoip2.record.Location;

import java.sql.Time;
import java.sql.Timestamp;


public class GeoLocation {

    protected CityResponse response;
    protected Country country;
    protected City city;
    protected String iso_code;
    protected String country_name;
    protected String city_name;
    protected Location location;
    private Double latitude;
    private Double longitude;

    private Timestamp last_scanned;
    private String hostname;

    public GeoLocation(CityResponse response, Timestamp last_scanned, String hostname) {
        this.response = response;
        this.country = response.getCountry();
        this.iso_code = country.getIsoCode();
        this.country_name = country.getName();
        this.city = response.getCity();
        this.city_name = city.getName();
        this.location = response.getLocation();
        this.latitude = response.getLocation().getLatitude();
        this.longitude = response.getLocation().getLongitude();
        this.last_scanned = last_scanned;
        this.hostname = hostname;
    }

    public GeoLocation(CityResponse response, Timestamp last_scanned) {
        this.response = response;
        this.country = response.getCountry();
        this.iso_code = country.getIsoCode();
        this.country_name = country.getName();
        this.city = response.getCity();
        this.city_name = city.getName();
        this.location = response.getLocation();
        this.latitude = response.getLocation().getLatitude();
        this.longitude = response.getLocation().getLongitude();
        this.last_scanned = last_scanned;
    }

    public GeoLocation (Double latitude, Double longitude, Timestamp last_scanned){
        this.latitude = latitude;
        this.longitude = longitude;
        this.last_scanned = last_scanned;
    }

    @Override
    public String toString() {
        return "GeoLocation{" +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", last_scanned=" + last_scanned;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public City getCity() {
        return city;
    }

    public void setLast_scanned(Timestamp last_scanned) {
        this.last_scanned = last_scanned;
    }

    public Timestamp getLast_scanned(){
        return last_scanned;
    }

    public String getHostname(){
        return hostname;
    }

    public void setHostname(String hostname){
        this.hostname = hostname;
    }
}