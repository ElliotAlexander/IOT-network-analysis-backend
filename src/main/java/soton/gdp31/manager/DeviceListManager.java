package soton.gdp31.manager;

import soton.gdp31.cache.DeviceUpdateCache;
import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @Author Elliot Alexander
 *
 * DeviceListManager is responsible for managing in-memory representations of devices while the packet listener is running
 * Ths idea behind this is to minimize database operations - an in memory representation allows us to track recent
 * updates in real time, and only update the database when necessary.
 *
 *
 * This class works in close conjunction with DeviceWrapper.
 *
 */
public class DeviceListManager {

    private ArrayList<DeviceWrapper> device_list;
    private Connection c;

    /**
     *
     *
     * Constructor reloads previously seen device timestamps from the database
     * This allows us to 'carry on where we left off' when restarting.
     * @param db_connection_wrapper
     */
    public DeviceListManager(DBConnection db_connection_wrapper){
        device_list = new ArrayList<>();
        String query = "SELECT m.uuid, m.packet_count, m.https_packet_count, m.data_transferred, m.data_in, m.data_out, t.mx FROM ( " +
                "SELECT uuid, MAX(timestamp) AS mx " +
                "FROM backend.device_stats_over_time " +
                "GROUP BY uuid" +
            ") t JOIN backend.device_stats_over_time m ON m.uuid = t.uuid AND t.mx = m.timestamp;";
        try {
            this.c = db_connection_wrapper.getConnection();
            PreparedStatement preparedStatement = c.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                DeviceWrapper dw = new DeviceWrapper(rs.getBytes(1));
                dw.setPacketCount(rs.getInt(2));
                dw.setHttpsPacketCount(rs.getInt(3));
                dw.setDataTransferred(rs.getLong(3));
                dw.setDataIn(rs.getLong(4));
                dw.setDataOut(rs.getLong(5));
                device_list.add(dw);
            }
        } catch (SQLException | DBConnectionClosedException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDevice(byte[] uuid){
        for(DeviceWrapper w : device_list){
            if(Arrays.equals(w.getUUID(), uuid)){
                return true;
            }
        }
        return false;
    }

    public DeviceWrapper getDevice(byte[] uuid) {
        return device_list.stream().filter(d -> Arrays.equals(d.getUUID(),uuid)).collect(Collectors.toCollection(ArrayList::new)).get(0);
    }

    public DeviceWrapper addDevice(byte[] uuid){
        DeviceWrapper w = new DeviceWrapper(uuid);
        w.setLastUpdateTime(System.currentTimeMillis());
        device_list.add(w);
        return w;
    }

    public boolean updateStats(PacketWrapper p) {
        DeviceWrapper wrapper = this.getDevice(p.getUUID());
        wrapper.setPacketCount(wrapper.getPacketCount() + 1);
        if(p.isHTTPS()){
            wrapper.setHttpsPacketCount(wrapper.getHttpsPacketCount() + 1);
        }
        return true;
    }

}
