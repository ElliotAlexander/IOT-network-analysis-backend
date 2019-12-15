
package soton.gdp31.database;

import soton.gdp31.cache.DeviceUpdateCache;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.cache.DeviceUUIDCache;
import soton.gdp31.wrappers.DeviceWrapper;
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

    public void addDeviceToDatabase(PacketWrapper p){
        if(!DeviceUUIDCache.DeviceObjectCacheInstance(database_connection_handler).checkDeviceExists(p.getUUID())) {
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
                preparedStatement.setTimestamp(6, new Timestamp(
                        ZonedDateTime.now().toInstant().toEpochMilli()
                ));

                preparedStatement.setTimestamp(7, new Timestamp(
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
            } catch (SQLException e) {
                new DBExceptionHandler(e, database_connection_handler);
            }
        }
    }

    public void updateDeviceTimestamp(PacketWrapper p){
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



    public void updateDeviceStats(DeviceWrapper device_wrapper, long timestamp){
        String insert_query = "INSERT INTO packet_counts_over_time(uuid, timestamp, packet_count, https_packet_count) VALUES(?,?,?,?)";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setBytes(1, device_wrapper.getUUID());
            preparedStatement.setTimestamp(2, new Timestamp(
                    timestamp
            ));
            preparedStatement.setLong(3, device_wrapper.getPacketCount());
            preparedStatement.setLong(4, device_wrapper.getPacketCount());
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }


}
