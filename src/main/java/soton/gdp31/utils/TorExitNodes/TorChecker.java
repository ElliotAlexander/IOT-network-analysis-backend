package main.java.soton.gdp31.utils.TorExitNodes;

import main.java.soton.gdp31.database.DBTorHandler;

import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

public class TorChecker {

    private ArrayList<String> ipAddreses;
    private ArrayList<String> caughtIps;

    public TorChecker(){

        this.ipAddreses = generateNodeList();
        this.caughtIps = new ArrayList<>();
    }


    public ArrayList<String> generateNodeList(){
        try {
            File file = new File("tmp/exitnodes.txt");
            FileReader fileReader = new FileReader(file);
            BufferedReader br = new BufferedReader(fileReader);

            ArrayList<String> ipAddresses = new ArrayList<>();
            String line;
            while((line = br.readLine()) != null){
                // process the line.
                ipAddresses.add(line);
            }

            return ipAddresses;
        } catch (FileNotFoundException e){
            e.printStackTrace();
            return null;
        } catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }

    public boolean checkNodeList(String ipAddress) {
        if(caughtIps.contains(ipAddress)){
            return false;
        }

        boolean flag = this.ipAddreses.contains(ipAddress);

        if(flag){
            caughtIps.add(ipAddress);
        }

        return flag;
    }

    /*
    public boolean checkNodeList(String ip_address) {
        if(ip_address == null) {
            soton.gdp31.logger.Logging.logInfoMessage("Null IP");
            return false;
        }

        soton.gdp31.logger.Logging.logInfoMessage("LOCATIONABLE IP: " + ip_address);
        try {
            byte[] address = InetAddress.getByName(ip_address).getAddress();
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                byte[] exit_node = InetAddress.getByName(line).getAddress();
                soton.gdp31.logger.Logging.logInfoMessage("ADDR:" + address.toString());
                soton.gdp31.logger.Logging.logInfoMessage("LINE:" + exit_node.toString());
                boolean flag = exit_node == address;

                soton.gdp31.logger.Logging.logInfoMessage("FLAG: " + flag);
                if (flag) {
                    soton.gdp31.logger.Logging.logInfoMessage("TOR EXIT NODE FOUND: " + ip_address);
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            soton.gdp31.logger.Logging.logInfoMessage("File not found");
            return false;
        }
        //soton.gdp31.logger.Logging.logInfoMessage("Exit node not found");

        return false;
    }


     */
}