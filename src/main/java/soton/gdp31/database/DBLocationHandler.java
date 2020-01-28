package soton.gdp31.database;

import java.time.ZonedDateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import main.java.soton.gdp31.utils.TorChecker.TorChecker;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.utils.GeoIpLocation.GeoLocation;
import soton.gdp31.utils.GeoIpLocation.LocationFinder;

public class DBLocationHandler {

    private final soton.gdp31.database.DBConnection database_connection_handler;
    private Connection c;
    private LocationFinder finder;
    private main.java.soton.gdp31.utils.TorChecker.TorChecker torChecker;

    public DBLocationHandler(soton.gdp31.database.DBConnection database_connection_handler) throws DBConnectionClosedException {
        this.database_connection_handler = database_connection_handler;
        this.c = database_connection_handler.getConnection();
        this.finder = new LocationFinder();
        this.torChecker = new TorChecker();
    }

    // Currently not used, as batch update from cache.
    public void addToDatabase(byte[] uuid, String ipAddress, GeoLocation geoLocation, Boolean torNode) {
        final Double LATITUDE = geoLocation.getLatitude();
        final Double LONGITUDE = geoLocation.getLongitude();




        try {
            String insert_query = "INSERT INTO ip_address_location(" +
                    "uuid, ip_address, latitude, longitude, last_scanned, is_tor_node" +
                    " VALUES(?,?,?,?,?,?)";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setBytes(1, uuid);
            preparedStatement.setString(2, ipAddress);
            preparedStatement.setDouble(3, LATITUDE);
            preparedStatement.setDouble(4, LONGITUDE);
            preparedStatement.setTimestamp(5, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setBoolean(6, torNode);

            preparedStatement.executeQuery();
        } catch (SQLException e) {
            new soton.gdp31.database.DBExceptionHandler(e, database_connection_handler);
        }
    }


    public void update(String ipAddress, GeoLocation geoLocation) {
        // Location is added here.
        final Double LATITUDE = geoLocation.getLatitude();
        final Double LONGITUDE = geoLocation.getLongitude();

        try {
            String update_query = "UPDATE ip_address_location SET last_scanned = ?, latitude = ?, longitude = ? WHERE ip_address = ?";
            PreparedStatement preparedStatement = c.prepareStatement(update_query);
            preparedStatement.setTimestamp(1, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setDouble(2, LATITUDE);
            preparedStatement.setDouble(3, LONGITUDE);
            preparedStatement.setString(4, ipAddress);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            new soton.gdp31.database.DBExceptionHandler(e, database_connection_handler);
        }
    }

    public void addTorNode(byte[] uuid, String ipAddress, Boolean torNode) {

        try{
            String insert_query = "INSRET INTO ip_address_location(" +
            "uuid, ip_address, is_tor_node) " +
                    "VALUES(?,?,?) " +
                    "ON CONFLICT (uuid, ip_address) " +
                    "DO UPDATE SET is_tor_node = ?";

            PreparedStatement ps = c.prepareStatement(insert_query);

            ps.setBytes(1, uuid);
            ps.setString(2, ipAddress);
            ps.setBoolean(3, torNode);
            ps.setBoolean(4, torNode);

            ps.executeQuery();
            soton.gdp31.logger.Logging.logInfoMessage("Inserted Tor Exit Node detection for: " + ipAddress);
        } catch (SQLException e) {
            soton.gdp31.logger.Logging.logInfoMessage("Failed to insert Tor Exit Node detection for: " + ipAddress);
            e.printStackTrace();
        }
    }
}