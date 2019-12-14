package soton.gdp31.database;

import soton.gdp31.exceptions.database.*;
import soton.gdp31.utils.NetworkUtils.ScanOutcome;
import soton.gdp31.cache.DeviceUUIDCache;
import soton.gdp31.wrappers.PacketWrapper;
import soton.gdp31.logger.*;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Class to input the ScanOutcome object into the database and retrieve
 * information.
 * 
 * @author Elliott Power
 */

public class DBPortHandler {

    private final DBConnection database_connection_handler;
    private Connection c;

    public DBPortHandler(DBConnection db_connection_handler) throws DBConnectionClosedException {
        this.database_connection_handler = db_connection_handler;
        this.c = database_connection_handler.getConnection();
    }

    public void commitScanOutcomeToDatabase(ScanOutcome o) {

        // Device is in main database
        if (deviceExistsInMain(o.getUUID())) {
            // Is device in PortScanning database?
            if (deviceExistsInPortDB(o.getUUID())) {
                // Update stats.
                updateTCPPortList(o.getTCPPorts(), o.getUUID());
                updateUDPPortList(o.getUDPPorts(), o.getUUID());
            } else { // Device not in PortScanning database.
                addDeviceToDatabase(o);
            }
        }
    }

    private void addDeviceToDatabase(ScanOutcome o) {
        try {
            String insert_query = "INSERT INTO port_scanning(" +
                    "uuid,open_tcp_ports,open_udp_ports,last_scanned)" +
                    " VALUES(?,?,?,?);";
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);

            preparedStatement.setBytes(1, o.getUUID());
            preparedStatement.setString(2, convertPortsToString(o, "TCP"));
            preparedStatement.setString(3, convertPortsToString(o, "UDP"));
            preparedStatement.setTimestamp(4,     new Timestamp(
                ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.execute();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    /**
     * Update the TCP list in the port_scanning table for a given UUID.
     */
    private void updateTCPPortList(List<Integer> ports, byte[] uuid) {
        String insert_query = "UPDATE port_scanning SET open_tcp_ports = ?, last_scanned = ? WHERE uuid = ?";

        try {
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setString(1, convertPortsToString(ports));
            preparedStatement.setTimestamp(2,     new Timestamp(
                ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setBytes(3, uuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    /**
     * Update the UDP list in the port_scanning table for a given UUID.
     */
    private void updateUDPPortList(List<Integer> ports, byte[] uuid) {
        String insert_query = "UPDATE port_scanning SET open_udp_ports = ?, last_scanned = ? WHERE uuid = ?";

        try {
            PreparedStatement preparedStatement = c.prepareStatement(insert_query);
            preparedStatement.setString(1, convertPortsToString(ports));
            preparedStatement.setTimestamp(2,     new Timestamp(
                ZonedDateTime.now().toInstant().toEpochMilli()
            ));
            preparedStatement.setBytes(3, uuid);
            preparedStatement.executeUpdate();
        } catch (SQLException e){
            new DBExceptionHandler(e, database_connection_handler);
        }
    }

    /**
     * Checks the main database for a device with the uuid provided
     */
    private boolean deviceExistsInMain(byte[] uuid){
        if(DeviceUUIDCache.DeviceObjectCacheInstance(database_connection_handler).checkDeviceExists(uuid, "device_stats")) { // TODO: Check this is device_stats and not devices
            return true;
        } else {    /// Device doesn't exist
            return false;
        }
    }

    /**
     * Checks the PortScanning database for a device with the uuid provided
     */
    private boolean deviceExistsInPortDB(byte[] uuid){
        if(DeviceUUIDCache.DeviceObjectCacheInstance(database_connection_handler).checkDeviceExists(uuid, "port_scanning")) {
            return true;
        } else {    /// Device doesn't exist
            return false;
        }
    }

    /**
     * Helper to convert ScanOutcome object to a string containing comma seperated values of open ports.
     */
    private String convertPortsToString(ScanOutcome o, String type) {
        List<Integer> ports = new ArrayList<Integer>();

        if (type.equalsIgnoreCase("TCP")){
            ports = o.getTCPPorts();
        } else if (type.equalsIgnoreCase("UDP")){
            ports = o.getUDPPorts();
        } else {
            Logging.logErrorMessage("Database Port Handler: Wrong string type passed to convertPortsToString function. Must of type UDP or TCP.");
        }

        StringBuilder strbul = new StringBuilder();

        Iterator<Integer> iter = ports.iterator();
        while(iter.hasNext()) {
            strbul.append(iter.next());
            if(iter.hasNext()){
                strbul.append(",");
            }
        }
        String csv = strbul.toString();
        return csv; // Comma seperated string of ports e.g. 
    }

    private String convertPortsToString(List<Integer> ports) {
        StringBuilder strbul = new StringBuilder();

        Iterator<Integer> iter = ports.iterator();
        while(iter.hasNext()) {
            strbul.append(iter.next());
            if(iter.hasNext()){
                strbul.append(",");
            }
        }
        String csv = strbul.toString();
        return csv; // Comma seperated string of ports e.g. 
    }
    
    /**
     * Helper to convert CSV to List<Integer>
     */
    private List<Integer> convertStringtoPorts(String csv) {
        List<Integer> list = Stream.of(csv.split(","))
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        return list;
    }
 }