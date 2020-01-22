package soton.gdp31.cache;

import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.database.DBExceptionHandler;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.GeoIpLocation.GeoLocation;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.*;
import java.security.NoSuchAlgorithmException;

public class GeoLocationCache {

    // Database connection.
    private DBConnection database_connection_handler;
    private Connection connection;
    private Long last_pushed;

    // Destination address cache. IP address -> Last seen epoch time.

    // UUID ->
    HashMap<byte[], HashMap<String, GeoLocation>> address_location_cache;

    // Cache timeout in milliseconds.
    public static final int CACHE_TIMEOUT = 15000;

    // Cache pushes every 10 seconds.
    public static final int DATABASE_UPDATE_TIMER = 10000;

    private static boolean instantiated;
    private static GeoLocationCache address_location_cache_instance;

    public static GeoLocationCache GeoLocationCacheInstance(DBConnection c){
        if(!instantiated){
            try {
                address_location_cache_instance = new GeoLocationCache(c);
            } catch (DBConnectionClosedException e){
                Logging.logErrorMessage("Failed to instantiate Geo Location Cache instance. Exiting.");
                e.printStackTrace();
            }
            instantiated = true;
        }
        return address_location_cache_instance;
    }

    private GeoLocationCache(DBConnection database_connection_handler) throws DBConnectionClosedException {
        address_location_cache = new HashMap<byte[], HashMap<String, soton.gdp31.utils.GeoIpLocation.GeoLocation>>();
        this.database_connection_handler = database_connection_handler;
        this.connection = database_connection_handler.getConnection();
        this.last_pushed = System.currentTimeMillis();
    }

    public boolean needsLocating(byte[] given_uuid, String given_ip_address){
        // Check cache for any instance of ip_address.
        ArrayList devices_contacted_ip = new ArrayList<byte[]>();

        for (Map.Entry uuids : address_location_cache.entrySet()) {
            byte[] uuid = (byte[]) uuids.getKey();

            HashMap<String, soton.gdp31.utils.GeoIpLocation.GeoLocation> devices_addresses = (HashMap<String, soton.gdp31.utils.GeoIpLocation.GeoLocation>) uuids.getValue();
            for(Map.Entry address_locations : devices_addresses.entrySet()) {
                String ip_address = (String) address_locations.getKey();
                soton.gdp31.utils.GeoIpLocation.GeoLocation location = (soton.gdp31.utils.GeoIpLocation.GeoLocation) address_locations.getValue();

                if(given_ip_address.equals(ip_address)){
                    devices_contacted_ip.add(uuid);
                }
            }
        }

        // If not found, check the database for any instance of ip address.
        Boolean database_has_ip;

        if(devices_contacted_ip.isEmpty()){
            // Check database.
            String query = "SELECT latitude, longitude, last_scanned FROM ip_address_location WHERE ip_address = ?";
            try {
                PreparedStatement ps = database_connection_handler.getConnection().prepareStatement(query);
                ps.setString(1, given_ip_address);
                ResultSet rs = ps.executeQuery();
                if(rs.next() == false){
                    database_has_ip = false;
                } else {
                    Double latitude = rs.getDouble("latitude");
                    Double longitude = rs.getDouble("longitude");
                    Long last_scanned = rs.getLong("last_scanned");

                    GeoLocation from_database = new GeoLocation(latitude, longitude, last_scanned);

                    // Place this into the cache.
                    HashMap<String, GeoLocation> uuids_list = address_location_cache.get(given_uuid);
                    uuids_list.put(given_ip_address, from_database);

                    database_has_ip =  true;
                }
            } catch (SQLException e) {
                new DBExceptionHandler(e, database_connection_handler);
                database_has_ip = false;
            } catch (DBConnectionClosedException e) {
                new DBExceptionHandler(e, database_connection_handler);
                database_has_ip = false;
            }

            if (database_has_ip) {
                // Database does have a record of this ip address, this has now been placed into the cache.
                return false;
            } else {
                return true;
            }
        } else {
            // Cache does have a record of this ip address.
            // Check to see if the records, match any traffic between given UUID device and cache UUID.
            if(devices_contacted_ip.contains(given_uuid)){
                return false;
            } else {
                return true;
            }
        }
        // Get list of all ip_addresses.

        // Check that the device uuid matches any of those rows.

        // If it does, return true.

        // If it does not, return false.
    }


    public void storeLocation(byte[] device_uuid, String ip_address, GeoLocation location){
        // Check device exists in the cache.
        if(address_location_cache.containsKey(device_uuid)){
            // Device exists.
            // Check ip_address doesnt already exist.
            HashMap<String, GeoLocation> uuid_locations = address_location_cache.get(device_uuid);

            if(uuid_locations.containsKey(ip_address)){
                // Device already has ip_address.

                // Update location.
                uuid_locations.replace(ip_address, location);
            } else {
                // Device doesn't have ip address.
                uuid_locations.put(ip_address, location);
            }
        } else {
            // Device does not exist.

            // Add the device.
            // Instantiate new hashmap for the device inside.
            HashMap<String, GeoLocation> uuid_locations = new HashMap<>();
            // Add first location.
            uuid_locations.put(ip_address, location);

            // Store on cache.
            address_location_cache.put(device_uuid, uuid_locations);
            //Logging.logWarnMessage("GeoLocation Cache does not contain device UUID: " + device_uuid);
        }

        pushCacheToDatabase();
    }


    public boolean pushCacheToDatabase(){
        Long currentTime = System.currentTimeMillis();
        if(this.last_pushed < currentTime - DATABASE_UPDATE_TIMER){
            // Update database.
            for(Map.Entry<byte[], HashMap<String, GeoLocation>> first : address_location_cache.entrySet()) {
                byte[] uuid = first.getKey();
                HashMap<String, GeoLocation> address_and_location = first.getValue();

                for(Map.Entry<String, GeoLocation> second : address_and_location.entrySet()){
                    String ip_address = second.getKey();
                    GeoLocation location = second.getValue();

                    Double latitude = location.getLatitude();
                    Double longitude = location.getLongitude();
                    Long last_scanned = location.getLast_scanned();

                    String query =
                            "BEGIN" +
                                "IF NOT EXISTS (SELECT * FROM ip_address_location" +
                                                "WHERE uuid = ?" +
                                                "AND ip_address = ?)" +
                                "BEGIN" +
                                    "INSERT INTO ip_address_location (uuid, ip_address, latitude, longitude, last_scanned)" +
                                    "VALUES (?,?,?,?,?)" +
                                "END"+
                            "END";
                    try {
                        PreparedStatement ps = database_connection_handler.getConnection().prepareStatement(query);

                        ps.setBytes(1, uuid);
                        ps.setString(2, ip_address);
                        ps.setBytes(3, uuid);
                        ps.setString(4, ip_address);
                        ps.setDouble(5, latitude);
                        ps.setDouble(6, longitude);
                        ps.setLong(7, last_scanned);

                        ResultSet rs = ps.executeQuery();

                    } catch (DBConnectionClosedException e) {
                        new DBExceptionHandler(e, database_connection_handler);
                        return false;
                    } catch (SQLException e) {
                        new DBExceptionHandler(e, database_connection_handler);
                        return false;
                    }
                }
            }
            this.last_pushed = System.currentTimeMillis();
            return true;
        }
        // else do nothing.
        return false;
    }



    // TODO: Create pruning cache thread. Checks if device is still on last_seen deviceUUIDcache, if no, then kick off GeoLocationCache.
    // TODO: GeoLocationCache assumes that all devices are on DeviceUUIDCache.
    // TODO: Devices are added to GeoLocationCache when also added to the DeviceUUIDCache.
    // Check if a device exists in a database
    public boolean check_device_exists_has_ip_address(byte[] uuid, String ip_address){
        if(address_location_cache.containsKey(uuid)){
            HashMap<String, soton.gdp31.utils.GeoIpLocation.GeoLocation> list_of_locations = address_location_cache.get(uuid);// returns <IP Address, Location Object> hashmap

            if(list_of_locations.containsKey(ip_address)){
                if(list_of_locations.get(ip_address).getLast_scanned() < (System.currentTimeMillis() - CACHE_TIMEOUT)){
                    list_of_locations.remove(ip_address);
                }
                return true;
            } else {
                // Has this device been scanned before?
                String query = "SELECT ip_address FROM ip_address_location WHERE ip_address = ?";
                try {
                    PreparedStatement ps = database_connection_handler.getConnection().prepareStatement(query);
                    ps.setBytes(1, ip_address.getBytes());
                    ResultSet rs = ps.executeQuery();
                    if(rs.next() == false){
                        return false;
                    } else {
                        return true;
                    }
                } catch (SQLException e) {
                    new DBExceptionHandler(e, database_connection_handler);
                    return false;
                } catch (DBConnectionClosedException e) {
                    new DBExceptionHandler(e, database_connection_handler);
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    public boolean addIPAddress(byte[] uuid, String ip_address, soton.gdp31.utils.GeoIpLocation.GeoLocation location){
        try {
            if(address_location_cache.containsKey(uuid)){
                HashMap<String, soton.gdp31.utils.GeoIpLocation.GeoLocation> list_of_locations = address_location_cache.get(uuid); // Get list of ip_address -> locations

                if(list_of_locations.containsKey(ip_address)){
                    // Device has seen this IP address before.
                    soton.gdp31.utils.GeoIpLocation.GeoLocation location_object = list_of_locations.get(ip_address); // GeoLocation of ip_address in Cache.
                    Long last_scanned = location_object.getLast_scanned(); // Last time GeoLocation was scanned.
                    if(last_scanned < (System.currentTimeMillis() - CACHE_TIMEOUT)){
                        // TODO: Remove location if it was scanned ages ago?
                    }
                    // Replace old cache of ip address with new one.
                    // TODO: Maybe a chokepoint, if you refreshing here.????
                    //GeoLocation refreshed_location = LookUpService.lookup(ip_address)
                    list_of_locations.replace(ip_address, location);
                    return true;
                } else {
                    // Device hasn't seen this IP address before.
                    list_of_locations.put(ip_address, location);
                    return true;
                }
            } else {
                return false;
                // Device isn't in Cache. TODO: Pruner should mean that this almost never happens?
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
