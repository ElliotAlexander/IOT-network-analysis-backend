package soton.gdp31.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;

public class DBScanHandler {
    private final soton.gdp31.database.DBConnection database_connection_handler;
    private Connection c;

    public DBScanHandler(soton.gdp31.database.DBConnection database_connection_handler) throws soton.gdp31.exceptions.database.DBConnectionClosedException {
        this.database_connection_handler = database_connection_handler;
        this.c = database_connection_handler.getConnection();
    }

    public void addToDatabase(byte[] uuid, String list_of_ports){
        // Adds a string of open ports, delimited by a comma.
        // e.g. "2,54,4380,10000," shows that ports 2, 54, 4380 and 10,000 are open.
        try {
            String insert_query = "INSERT INTO backend.port_scanning(" +
                    "uuid, open_tcp_ports, last_scanned)" +
                    " VALUES(?,?,?);";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setBytes(1, uuid);
            preparedStatement.setString(2, list_of_ports);
            preparedStatement.setTimestamp(3, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.execute();
            soton.gdp31.logger.Logging.logInfoMessage("Inserted port scanning data for Device: " + uuid.toString());
        } catch (SQLException e) {
            new soton.gdp31.database.DBExceptionHandler(e, database_connection_handler);
        }

    }

    public void updateDatabase(byte[] uuid, String list_of_ports) {
        try {
            String insert_query = "UPDATE backend.port_scanning" +
                    " SET open_tcp_ports = ?, last_scanned = ?" +
                    " WHERE uuid = ?";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setBytes(3, uuid);
            preparedStatement.setString(1, list_of_ports);
            preparedStatement.setTimestamp(2, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.execute();
            soton.gdp31.logger.Logging.logInfoMessage("Updated port scanning database for Device: " + uuid.toString());
        } catch (SQLException e) {
            new soton.gdp31.database.DBExceptionHandler(e, database_connection_handler);
        }
    }
}