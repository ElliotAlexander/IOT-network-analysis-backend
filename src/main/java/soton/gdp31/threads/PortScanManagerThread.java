package soton.gdp31.threads;

import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBScanHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.logger.Logging;
import soton.gdp31.utils.PortScanning.PortScanResult;
import soton.gdp31.utils.PortScanning.PortScanner;
import soton.gdp31.utils.ScanProcessingQueue;
import soton.gdp31.wrappers.PacketWrapper;

import java.util.ArrayList;

public class PortScanManagerThread extends Thread {
    DBScanHandler scan_database_handler;
    private DBConnection connection_handler;

    public PortScanManagerThread() {
        Logging.logInfoMessage("Attempting to open database connection for Port Scanning Threads..");
        while(openConnections() == false){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Logging.logInfoMessage("Successfully opened database Connection for Port Scanning Threads.");
    }

    public boolean openConnections(){
        try {
            this.connection_handler = new DBConnection();
            this.scan_database_handler = new DBScanHandler(connection_handler);
            return true;
        } catch (DBConnectionClosedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void scanPorts(PacketWrapper p, boolean initial){
        PortScanner scanner = new PortScanner();

        // Get the IP address associated with a device.
        String device_ip_address = p.getAssociatedIpAddress().getHostAddress();

        ArrayList<PortScanResult> results = scanner.scanDevicePorts(device_ip_address);
        String string_for_db = scanner.extract_list_of_open_ports(results);

        if(initial){
            scan_database_handler.addToDatabase(p.getUUID(), string_for_db);
        } else {
            scan_database_handler.updateDatabase(p.getUUID(), string_for_db);
        }
    }

    public void run(){
        while(true){
            PacketWrapper p = ScanProcessingQueue.instance.scanQueue.poll();
            if(p == null || !p.isProcessable()){
                try {
                    Thread.sleep(50);
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Logging.logInfoMessage("Starting port scan for " + p.getAssociatedHostname());
            scanPorts(p, true);
        }


    }
}
