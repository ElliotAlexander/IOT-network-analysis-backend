
package soton.gdp31.database;

import soton.gdp31.logger.Logging;
import soton.gdp31.utils.DeviceVendor.VendorChecker;
import org.pcap4j.packet.DnsQuestion;
import org.pcap4j.util.Inet4NetworkAddress;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.cache.DeviceUUIDCache;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.Iterator;
import java.util.List;

/**
 * @Author Elliot Alexander
 * This class acts as a mapping for DeviceWrapper onto the database.
 * Elements of DeviceWrapper, or previously unseen devices, may be passed in here and updated in the database.
 * THis class has no internal state and acts as a
 */
public class DBDeviceHandler {

    private final DBConnection database_connection_handler;
    private Connection c;
    private VendorChecker vendorChecker;

    public DBDeviceHandler(DBConnection database_connection_handler) throws DBConnectionClosedException {
        this.database_connection_handler = database_connection_handler;
        this.c = database_connection_handler.getConnection();
        this.vendorChecker = new VendorChecker();
    }

    public void addToDatabase(PacketWrapper p){
        if(!DeviceUUIDCache.DeviceObjectCacheInstance(database_connection_handler).checkDeviceExists(p.getUUID())) {
            try {
                String insert_query = "INSERT INTO backend.devices(" +
                        "uuid,mac_addr,device_hostname,device_nickname,internal_ip_v4,internal_ip_v6,currently_active,last_seen,first_seen,device_type," +
                        "set_ignored)" + " VALUES(?,?,?,?,?,?,?,?,?,?,?);";
                PreparedStatement preparedStatement = c.prepareStatement(insert_query);
                preparedStatement.setBytes(1, p.getUUID());
                preparedStatement.setString(2, p.getAssociatedMacAddress());
                preparedStatement.setString(3, p.getAssociatedHostname());
                preparedStatement.setString(4, "not set");
                preparedStatement.setString(5, (p.isIPv6() ? "not set" : p.getSrcIp()));
                preparedStatement.setString(6, (p.isIPv6() ? p.getSrcIp() : "not set"));
                preparedStatement.setBoolean(7, true);
                preparedStatement.setTimestamp(8, new Timestamp(
                        ZonedDateTime.now().toInstant().toEpochMilli()
                ));

                preparedStatement.setTimestamp(9, new Timestamp(
                        ZonedDateTime.now().toInstant().toEpochMilli()
                ));
                preparedStatement.setBoolean(11, false);
                preparedStatement.setString(10, vendorChecker.checkMac(p.getAssociatedMacAddress()));
                preparedStatement.execute();

                String device_insert_query = "INSERT INTO backend.device_stats(uuid, packet_count, https_packet_count, data_in, data_out, data_transferred)" + "VALUES(?,?,?,?,?,?);";
                PreparedStatement device_stats_prepared_statement = c.prepareStatement(device_insert_query);
                device_stats_prepared_statement.setBytes(1, p.getUUID());
                device_stats_prepared_statement.setInt(2, 1);
                device_stats_prepared_statement.setInt(3, 1);
                device_stats_prepared_statement.setInt(4, 1);
                device_stats_prepared_statement.setInt(5, 1);
                device_stats_prepared_statement.setInt(6, p.getPacketSize());
                device_stats_prepared_statement.execute();


                String device_port_traffic_query = "INSERT INTO backend.device_stats";
            } catch (SQLException e) {
                new soton.gdp31.database.DBExceptionHandler(e, database_connection_handler);
            }
        }
    }

    public void updateLastSeen(DeviceWrapper device){
        try {
            String update_query = "UPDATE backend.devices SET last_seen = ? WHERE uuid = ?";
            PreparedStatement preparedStatement = c.prepareStatement(update_query);
            preparedStatement.setTimestamp(1, new Timestamp(
                    ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setBytes(2, device.getUUID());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    public void updateLivePacketCounts(DeviceWrapper device_wrapper, long timestamp){
        // Device stats over time
        String deviceStatsOverTimeInsertQuery = "INSERT INTO backend.device_stats_over_time(uuid, timestamp, packet_count, https_packet_count, data_transferred, data_in, data_out) VALUES(?,?,?,?,?,?,?)";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(deviceStatsOverTimeInsertQuery);
            preparedStatement.setBytes(1, device_wrapper.getUUID());
            preparedStatement.setTimestamp(2, new Timestamp(
                    timestamp
            ));
            preparedStatement.setLong(3, device_wrapper.getPacketCount());
            preparedStatement.setLong(4, device_wrapper.getPacketCount());
            preparedStatement.setLong(5, device_wrapper.getDataTransferred());
            preparedStatement.setLong(6, device_wrapper.getDataIn());
            preparedStatement.setLong(7, device_wrapper.getDataOut());

            preparedStatement.execute();

        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    public void updatePacketCounts(DeviceWrapper device_wrapper, long timestamp){

        /**
         * Update the device stats over time table
         *
         * We keep two counts - one general count (to avoid us having to search many rows for an overall view),
         * And a count recorded by time, to allow us to graph this data if we want.
         * Both tables need to be updated seperately.
         */

        // Device stats - general count.
        String deviceStatsUpdateQuery = "UPDATE backend.device_stats SET packet_count = ?, https_packet_count = ?, data_transferred = ?, data_in = ?, data_out = ?, ports_traffic = ? where uuid = ?";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(deviceStatsUpdateQuery);
            preparedStatement.setLong(1, device_wrapper.getPacketCount());
            preparedStatement.setLong(2, device_wrapper.getHttpsPacketCount());
            preparedStatement.setLong(3, device_wrapper.getDataTransferred());
            preparedStatement.setLong(4, device_wrapper.getDataIn());
            preparedStatement.setLong(5, device_wrapper.getDataOut());
            preparedStatement.setString(6, device_wrapper.getPortTrafficString());
            preparedStatement.setBytes(7, device_wrapper.getUUID());

            preparedStatement.executeUpdate();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    public void updateDNSQueries(DeviceWrapper device){

        if(device.getDNSQueries().isEmpty()){
            return;
        }

        byte[] uuid = device.getUUID();
        try {
            c.setAutoCommit(false);
            PreparedStatement prepStmt = c.prepareStatement(
                    "INSERT INTO backend.device_dns_storage(uuid, url) VALUES (?,?) ON CONFLICT DO NOTHING;");
            Iterator<DnsQuestion> it = device.getDNSQueries().iterator();
            while(it.hasNext()){
                DnsQuestion p = it.next();
                prepStmt.setBytes(1,uuid);
                prepStmt.setString(2,p.getQName().getName());
                prepStmt.addBatch();
            }

            int [] numUpdates=prepStmt.executeBatch();
            c.commit();
            c.setAutoCommit(true);
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getNextException());
        }
    }

    public void updateSumDeviceStats(long data_transferred, long data_in, long data_out){
        String deviceStatsOverTimeInsertQuery = "INSERT INTO backend.device_data_sum_over_time(timestamp, data_transferred, data_in, data_out) VALUES(?,?,?,?)";
        try {
            PreparedStatement preparedStatement = c.prepareStatement(deviceStatsOverTimeInsertQuery);
            preparedStatement.setTimestamp(1, new Timestamp(
                    System.currentTimeMillis()
            ));
            preparedStatement.setLong(2, data_transferred);
            preparedStatement.setLong(3, data_in);
            preparedStatement.setLong(4, data_out);
            preparedStatement.execute();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }

    }

    public void upateDeviceIp(byte[] uuid, InetAddress ip_address){
        String deviceStatsOverTimeInsertQuery = "UPDATE backend.devices SET internal_ip_v4 = ?, internal_ip_v6 = ? WHERE uuid = ?";
        boolean isIpv4 = ip_address instanceof Inet4Address;
        try {
            PreparedStatement preparedStatement = c.prepareStatement(deviceStatsOverTimeInsertQuery);
            preparedStatement.setString(1, isIpv4 ? ip_address.getHostAddress() : null);
            preparedStatement.setString(2, !isIpv4 ? null : ip_address.getHostAddress());
            preparedStatement.setBytes(3, uuid);
            preparedStatement.execute();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }
}
