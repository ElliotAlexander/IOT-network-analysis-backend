package main.java.soton.gdp31.threads;

import main.java.soton.gdp31.database.DBScanHandler;
import main.java.soton.gdp31.utils.PortScanning.PortScanResult;
import main.java.soton.gdp31.utils.PortScanning.PortScanner;
import soton.gdp31.utils.ScanProcessingQueue;
import soton.gdp31.wrappers.PacketWrapper;

import java.util.ArrayList;

public class PortScanManagerThread extends Thread {
    DBScanHandler scan_database_handler;
    private soton.gdp31.database.DBConnection connection_handler;

    public PortScanManagerThread() {
        while(openConnections() == false){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean openConnections(){
        try {
            this.connection_handler = new soton.gdp31.database.DBConnection();
            this.scan_database_handler = new DBScanHandler(connection_handler);
            return true;
        } catch (soton.gdp31.exceptions.database.DBConnectionClosedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void scanPorts(soton.gdp31.wrappers.PacketWrapper p, boolean initial){
        PortScanner scanner = new PortScanner();
        String device_ip_address = p.getSrcIp();

        ArrayList<PortScanResult> results = scanner.scan_device_ports(device_ip_address);
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
            soton.gdp31.logger.Logging.logInfoMessage("Manager starting scan on: " + p.getAssociatedHostname());
            scanPorts(p, true);
        }


    }
}
