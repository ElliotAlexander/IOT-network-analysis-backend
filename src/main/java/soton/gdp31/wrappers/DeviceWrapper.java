package soton.gdp31.wrappers;

import soton.gdp31.exceptions.database.DBUknownDeviceException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class DeviceWrapper {

    private final Connection connection;
    private byte uuid[];

    public DeviceWrapper(Connection connection, String device_name){
        this.connection = connection;
        try {
            this.uuid = MessageDigest.getInstance("MD5").digest(device_name.getBytes());
        } catch (NoSuchAlgorithmException e){

        }
    }


    public byte[] getUUID() throws DBUknownDeviceException {
        try {
            String stmt_sql = "SELECT ? FROM devices WHERE uuid=?";
            PreparedStatement stmt = connection.prepareStatement(stmt_sql);
            stmt.setBytes(1, uuid);
            stmt.setBytes(2, uuid);
            ResultSet rs = stmt.executeQuery();
            if(rs.wasNull()){
                throw new SQLException();
            } else {
                return this.uuid;
            }
        } catch(SQLException e){
            throw new DBUknownDeviceException();
        }
    }
}
