package soton.gdp31.database;

import soton.gdp31.logger.Logging;
import soton.gdp31.rating.DeviceRating;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.rating.DeviceRating;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.database.DBConnection;

import java.sql.*;

public class DBRatingHandler {
    private final DBConnection database_connection_handler;
    private Connection c;

    public DBRatingHandler(DBConnection database_connection_handler) throws DBConnectionClosedException {
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
                            "   SET https_rating = ?, " +
                            "       ports_rating = ?," +
                            "       upload_rating = ?," +
                            "       overall = ?;";

            PreparedStatement ps = c.prepareStatement(insert_query);
            // On insert.
            ps.setBytes(1, uuid);
            ps.setDouble(2, https);
            ps.setDouble(3, ports);
            ps.setDouble(4, upload);

            ps.setDouble(5, overall);

            // On update

            ps.setDouble(6, https);
            ps.setDouble(7, ports);
            ps.setDouble(8, upload);

            ps.setDouble(9, overall);

            ps.executeUpdate();

            Logging.logInfoMessage("Inserted rating for new device: " + deviceRating.getUuid().toString());
        } catch (SQLException e) {
            Logging.logErrorMessage("Failed to insert rating for new device.");
            e.printStackTrace();
        }
    }

    public String pullOpenPorts(DeviceWrapper dWrapper) {
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