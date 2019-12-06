package soton.gdp31.manager;

import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.devices.UnknownDeviceException;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.wrappers.PacketWrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class DeviceListManager {

    private ArrayList<DeviceWrapper> device_list;
    private Connection c;

    public DeviceListManager(DBConnection db_connection_wrapper){
        device_list = new ArrayList<>();
        String query = "SELECT uuid FROM devices;";
        try {
            this.c = db_connection_wrapper.getConnection();
            PreparedStatement preparedStatement = c.prepareStatement(query);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                device_list.add(new DeviceWrapper(rs.getBytes(1)));
            }
        } catch (SQLException | DBConnectionClosedException e) {
            e.printStackTrace();
        }
    }

    public boolean checkDevice(byte[] uuid){
        for(DeviceWrapper w : device_list){
            if(w.getUUID() == uuid){
                return true;
            }
        }
        return false;
    }

    public DeviceWrapper getDevice(byte[] uuid) {
        return device_list.stream().filter(d -> d.getUUID() == uuid).collect(Collectors.toCollection(ArrayList::new)).get(0);
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
