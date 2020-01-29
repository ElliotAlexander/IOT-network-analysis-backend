package main.java.soton.gdp31.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DBTorHandler {

    private final soton.gdp31.database.DBConnection database_connection_handler;
    private Connection c;
    private soton.gdp31.utils.GeoIpLocation.LocationFinder finder;
    private main.java.soton.gdp31.utils.TorExitNodes.TorChecker torChecker;

    public DBTorHandler(soton.gdp31.database.DBConnection database_connection_handler) throws soton.gdp31.exceptions.database.DBConnectionClosedException {
        this.database_connection_handler = database_connection_handler;
        this.c = database_connection_handler.getConnection();
        this.torChecker = new main.java.soton.gdp31.utils.TorExitNodes.TorChecker();
    }


    public void addTorNode(byte[] uuid, String ipAddress, Boolean torNode) {

        try{
            String insert_query =
                    "INSERT INTO backend.tor_nodes" +
                        "(uuid, ip_address, is_tor_node) " +
                        "VALUES(?,?,?) " +
                        "ON CONFLICT DO NOTHING; ";

            PreparedStatement ps = c.prepareStatement(insert_query);

            ps.setBytes(1, uuid);
            ps.setString(2, ipAddress);
            ps.setBoolean(3, torNode);

            ps.executeUpdate();

            soton.gdp31.logger.Logging.logInfoMessage("Inserted Tor Exit Node detection for: " + ipAddress);
        } catch (SQLException e) {
            soton.gdp31.logger.Logging.logInfoMessage("Failed to insert Tor Exit Node detection for: " + ipAddress);
            e.printStackTrace();
        }
    }
}
