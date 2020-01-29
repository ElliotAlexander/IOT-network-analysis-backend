package main.java.soton.gdp31.database;

import main.java.soton.gdp31.rating.DeviceRating;

import java.sql.*;

public class DBRatingHandler {
    private final soton.gdp31.database.DBConnection database_connection_handler;
    private Connection c;

    public DBRatingHandler(soton.gdp31.database.DBConnection database_connection_handler) throws soton.gdp31.exceptions.database.DBConnectionClosedException {
        this.database_connection_handler = database_connection_handler;
        this.c = database_connection_handler.getConnection();
    }


    public void addRating(DeviceRating deviceRating){
        try{
            byte[] uuid = deviceRating.getUuid();
            double https = deviceRating.getHttps_normalized();
            double ports = deviceRating.getOpen_ports_normalized();
            double upload = deviceRating.getUpload_normalized();
            double overall = deviceRating.getOverall_rating();

            String insert_query =
                    "INSERT INTO backend.device_security_rating" +
                            "(uuid, https_rating, ports_rating, upload_rating, " +
                            "overall) " +
                            "VALUES(?,?,?,?," +
                            "?) " +
                            "ON CONFLICT (uuid) DO UPDATE" +
                            "   SET https_rating = ? " +
                            "       ports_rating = ?" +
                            "       upload_rating = ?" +
                            "       overall = ?;";

            PreparedStatement ps = c.prepareStatement(insert_query);
            // On insert.
            ps.setBytes(1, uuid);
            ps.setDouble(2, https);
            ps.setDouble(3, ports);
            ps.setDouble(4, upload);

            ps.setDouble(5, overall);

            // On update

            ps.setBytes(6, uuid);
            ps.setDouble(7, https);
            ps.setDouble(8, ports);
            ps.setDouble(9, upload);

            ps.setDouble(10, overall);

            ps.executeUpdate();

            soton.gdp31.logger.Logging.logInfoMessage("Inserted rating for new device: " + deviceRating.getUuid().toString());
        } catch (SQLException e) {
            soton.gdp31.logger.Logging.logErrorMessage("Failed to insert rating for new device.");
            e.printStackTrace();
        }
    }

    public String pullOpenPorts(soton.gdp31.wrappers.DeviceWrapper dWrapper) {
        try {

            String select_query =
                    "SELECT uuid, open_tcp_ports FROM backend.port_scanning WHERE uuid = ?;";

            PreparedStatement ps = c.prepareStatement(select_query);

            ps.setBytes(1, dWrapper.getUUID());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String list_of_ports = rs.getString(2);
                return list_of_ports;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}