package soton.gdp31.database;

import soton.gdp31.exceptions.database.*;
import soton.gdp31.utils.NetworkUtils.ScanOutcome;
import soton.gdp31.cache.DeviceUUIDCache;
import soton.gdp31.wrappers.PacketWrapper;

import java.sql.*;
import java.time.ZonedDateTime;
import java.util.Map;

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
                updatePortList(o.getPorts());
                updateLastScanned(o.getTimeOfScan());
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
            preparedStatement.setArray(2, o.getTCPPorts());
            preparedStatement.setArray(3, o.getUDPPorts());
        }
    }

    private void updateLastScanned(long timeOfScan) {
    }

    private void updatePortList(Map<Integer, Boolean> ports) {
        
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
     * Helper to convert an List of integers to an Array for insertion into PostGresSQL
     */
    
 }