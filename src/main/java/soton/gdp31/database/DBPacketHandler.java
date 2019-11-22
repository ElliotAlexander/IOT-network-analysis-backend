
package soton.gdp31.database;

import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.cache.DeviceUUIDCache;
import soton.gdp31.wrappers.PacketWrapper;

import java.sql.*;
import java.time.ZonedDateTime;


public class DBPacketHandler {

    private final DBConnection database_connection_handler;
    private Connection c;

    public DBPacketHandler(DBConnection database_connection_handler) throws DBConnectionClosedException {
        this.database_connection_handler = database_connection_handler;
        this.c = database_connection_handler.getConnection();
    }


    public void commitPacketToDatabase(PacketWrapper p) {
        // Does the device exist in the database?
        // Having a cache allows us to know this without checking the database
        if(DeviceUUIDCache.DeviceObjectCacheInstance(database_connection_handler).checkDeviceExists(p.getUUID())) {
            updateDeviceTimestamp(p);
            updatePacketStats(p);
        } else {    /// Device doesn't exist
            addDeviceToDatabase(p);
        }
    }



    private void updateDeviceTimestamp(PacketWrapper p){
        try {
            String update_query = "UPDATE devices SET last_seen = ? WHERE uuid = ?";
            PreparedStatement preparedStatement = c.prepareStatement(update_query);
            preparedStatement.setTimestamp(1, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setBytes(2, p.getUUID());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    private void addDeviceToDatabase(PacketWrapper p){
        try {
            String insert_query = "INSERT INTO devices(" +
                    "uuid,device_hostname,device_nickname,internal_ip_v4,currently_active,last_seen,first_seen," +
                    "set_ignored)" + " VALUES(?,?,?,?,?,?,?,?);";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setBytes(1, p.getUUID());
            preparedStatement.setString(2, (p.isOutgoingTraffic() ? p.getSrcHostname() : p.getDestHostname()));
            preparedStatement.setString(3, "not set");
            preparedStatement.setString(4, p.getSrcIp());
            preparedStatement.setBoolean(5, true);
            preparedStatement.setTimestamp(6,     new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));

            preparedStatement.setTimestamp(7,     new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setBoolean(8, false);
            preparedStatement.execute();

            String device_insert_query = "INSERT INTO device_stats(uuid, packet_count, https_packet_count, data_in, data_out, data_transferred)" + "VALUES(?,?,?,?,?,?);";
            PreparedStatement device_stats_prepared_statement = c.prepareStatement(device_insert_query);
            device_stats_prepared_statement.setBytes(1, p.getUUID());
            device_stats_prepared_statement.setInt(2, 1);
            device_stats_prepared_statement.setInt(3, 1);
            device_stats_prepared_statement.setInt(4, 1);
            device_stats_prepared_statement.setInt(5, 1);
            device_stats_prepared_statement.setInt(6, p.getPacketSize());
            device_stats_prepared_statement.execute();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    private void updatePacketStats(PacketWrapper p){
        String insert_query = "UPDATE device_stats SET packet_count = packet_count + 1, data_transferred = data_transferred + ?";
        String isHTTPSUpdate = ",https_packet_count = https_packet_count + 1 ";
        String isOutgoingPacket = ",data_out = data_out + ? ";
        String isIncomingPacket = ",data_in = data_in + ? ";
        if(p.isHTTPS())
            insert_query += isHTTPSUpdate;

        insert_query += (p.isOutgoingTraffic() ? isOutgoingPacket : isIncomingPacket);
        insert_query += " WHERE uuid = ?";

        try {
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setInt(1, p.getPacketSize());
            preparedStatement.setInt(2, p.getPacketSize());
            preparedStatement.setBytes(3, p.getUUID());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }
}
