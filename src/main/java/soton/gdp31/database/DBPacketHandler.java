
package soton.gdp31.database;

import soton.gdp31.logger.Logging;
import soton.gdp31.utils.DeviceObjectCache;
import soton.gdp31.wrappers.PacketWrapper;

import java.sql.*;
import java.time.ZonedDateTime;


public class DBPacketHandler {


    public boolean commitPacketToDatabase(PacketWrapper p, Connection c) {
        // Does the device exist in the database?
        // Having a cache allows us to know this without checking the database
        if(DeviceObjectCache.device_object_cache.checkDeviceExists(p.getUUID(), c)) {
            updateDeviceTimestamp(p, c);
            updatePacketStats(p,c);
        } else {    /// Device doesn't exist
            addDeviceToDatabase(p, c);
        }
        return true;
    }



    private boolean updateDeviceTimestamp(PacketWrapper p, Connection c){
        try {
            String update_query = "UPDATE devices SET last_seen = ? WHERE uuid = ?";
            PreparedStatement preparedStatement = c.prepareStatement(update_query);
            preparedStatement.setTimestamp(1, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setBytes(2, p.getUUID());
            preparedStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean addDeviceToDatabase(PacketWrapper p, Connection c){
        try {
            String insert_query = "INSERT INTO devices(" +
                    "uuid,device_hostname,device_nickname,internal_ip_v4,currently_active,last_seen,first_seen," +
                    "set_ignored)" + " VALUES(?,?,?,?,?,?,?,?);";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setBytes(1, p.getUUID());
            preparedStatement.setString(2, p.getHostname());
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

            String device_insert_query = "INSERT INTO device_stats(uuid, packet_count, data_transferred)" + "VALUES(?,?,?);";
            PreparedStatement device_stats_prepared_statement = c.prepareStatement(device_insert_query);
            device_stats_prepared_statement.setBytes(1, p.getUUID());
            device_stats_prepared_statement.setInt(2, 1);
            device_stats_prepared_statement.setInt(3, p.getPacketSize());
            device_stats_prepared_statement.execute();
            return true;
        } catch (SQLException e){
            return false;
        }
    }

    private boolean updatePacketStats(PacketWrapper p, Connection c){
        try {
            Logging.logInfoMessage("Updating device stats");
            String insert_query = "UPDATE device_stats SET packet_count = packet_count + 1, data_transferred = data_transferred + ? WHERE uuid = ?";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setInt(1, p.getPacketSize());
            preparedStatement.setBytes(2, p.getUUID());
            preparedStatement.executeQuery();
            return true;
        } catch (SQLException e){
            return false;
        }
    }
}
