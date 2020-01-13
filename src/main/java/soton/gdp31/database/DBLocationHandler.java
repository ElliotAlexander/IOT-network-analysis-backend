package soton.gdp31.database;


public class DBLocationHandler {

    private final DBConnection database_connection_handler;
    private connection c;

    public DBLocationHandler(DBConnection database_connection_handler) throws DBConnectionClosedException {
        this.database_connection_handler = database_connection_handler;
        this.c = database_connection_handler.getConnection();
    }

    public void addToDatabase(String ipAddress) {
        // Location is added here.
        GeoLocation location = LookUpService.lookup(ipAddress);
        final float LATITUDE = location.latitude;
        final float LONGITUDE = location.longitude;




        try {
            String insert_query = "INSERT INTO ip_address_location(" +
                    "ip_address, latitude, longitude, last_scanned" +
                    " VALUES(?,?,?,?)";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setString(1, ipAddress);
            preparedStatement.setLong(2, (long) LATITUDE);
            preparedStatement.setLong(3, (long) LONGITUDE);
            preparedStatement.setTimestamp(4, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
        } catch (SQLException e) {
            new DBExceptionHandler(e, database_connection_handler);
        }
    }


    public void update(String ipAddress) {

        GeoLocation location = LookUpService.lookup(ipAddress);
        final float LATITUDE = location.latitude;
        final float LONGITUDE = location.longitude;

        try {
            String update_query = "UPDATE ip_address_location SET last_scanned = ?, latitude = ?, longitude = ? WHERE ip_address = ?";
            PreparedStatement preparedStatement = c.prepareStatement(update_query);
            preparedStatement.setTimestamp(1, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setLong(2, (long) LATITUDE);
            preparedStatement.setLong(3, (long) LONGITUDE);
            preparedStatement.setString(4, ipAddress);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            new DBExceptionHandler(e, database_connection_handler);
        }
    }
}