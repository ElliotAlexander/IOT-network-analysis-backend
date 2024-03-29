package soton.gdp31.utils.GeoIpLocation;

import soton.gdp31.logger.Logging;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.*;


import java.io.*;
import java.io.IOException;
import java.net.*;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class LocationFinder {

    /*
    protected File database = new File(
            getClass().getClassLoader().getResource("GeoLite2-City.mmdb").getFile()
	);

     */
    protected File database = new File("tmp/GeoLite2-City.mmdb");
    protected com.maxmind.geoip2.DatabaseReader reader;
    protected InetAddress ipAddress;

    public LocationFinder() {
        try {
            this.reader = new com.maxmind.geoip2.DatabaseReader.Builder(database).build();
        } catch (IOException e) {
            Logging.logWarnMessage("Failed to load GeoIP2-Lite City Database. GeoLocation will not work.");
            e.printStackTrace();
        }
    }

    public GeoLocation lookup(String ipAddress){

        try {
            InetAddress ip_address = InetAddress.getByName(ipAddress);
            CityResponse response = reader.city(ip_address);

            Country country = response.getCountry();
            String iso_code = country.getIsoCode();
            String country_name = country.getName();

            City city = response.getCity();
            String city_name = city.getName();

            Location location = response.getLocation();
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();


            Timestamp last_scanned = new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            );
            // Build returnable.
            GeoLocation returnable = new GeoLocation(response, last_scanned);

            return returnable;
        } catch (UnknownHostException uhe){
            Logging.logWarnMessage("Failed to location host.");
            uhe.printStackTrace();
        } catch (IOException ioe) {
            Logging.logWarnMessage("Reading the City Database has failed.");
            ioe.printStackTrace();
        } catch (GeoIp2Exception geoipe) {
            try {
                if(InetAddress.getByName(ipAddress).isAnyLocalAddress()){
                    Logging.logInfoMessage("Attempted to GeoLocate a local address.");
                } else {
                    Logging.logInfoMessage("GeoIP2 Exception has been thrown");
                    geoipe.printStackTrace();
                }
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            // TODO: Failed GeoLocation.
        }
        return null;
    }

    public GeoLocation lookup(InetAddress ip_address){

        try {
            CityResponse response = reader.city(ip_address);

            Country country = response.getCountry();
            String iso_code = country.getIsoCode();
            String country_name = country.getName();

            City city = response.getCity();
            String city_name = city.getName();

            Location location = response.getLocation();
            Double latitude = location.getLatitude();
            Double longitude = location.getLongitude();


            Timestamp last_scanned = new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            );
            // Build returnable.
            GeoLocation returnable = new GeoLocation(response, last_scanned);
            Logging.logInfoMessage("GeoLocated an address at: " + ip_address.getHostName());
            return returnable;
        } catch (UnknownHostException uhe){
            Logging.logWarnMessage("Failed to location host.");
            uhe.printStackTrace();
        } catch (IOException ioe) {
            Logging.logWarnMessage("Reading the City Database has failed.");
            ioe.printStackTrace();
        } catch (GeoIp2Exception geoipe) {
            if(ip_address.isAnyLocalAddress()){
                Logging.logInfoMessage("Attempted to GeoLocate a local address.");
            } else {
                Logging.logInfoMessage("GeoIP2 Exception has been thrown");
                geoipe.printStackTrace();
            }
            // TODO: Failed GeoLocation.
        }
        return null;
    }

}