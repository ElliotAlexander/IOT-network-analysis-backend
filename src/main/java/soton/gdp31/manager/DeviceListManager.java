package soton.gdp31.manager;

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

public class DeviceListManager {

    private ArrayList<DeviceWrapper> device_list;
    private Connection c;

    public DeviceListManager(DBConnection db_connection_wrapper){
        device_list = new ArrayList<>();
        String query = "SELECT uuid,packet_count,https_packet_count FROM packet_counts_over_time;";
        try {
            this.c = db_connection_wrapper.getConnection();
            PreparedStatement preparedStatement = c.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                DeviceWrapper dw = new DeviceWrapper(rs.getBytes(1));
                dw.setPacketCount(rs.getInt(2));
                dw.setHttpsPacketCount(rs.getInt(3));
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
