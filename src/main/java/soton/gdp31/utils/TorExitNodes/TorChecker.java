package main.java.soton.gdp31.utils.TorChecker;

import java.io.*;
import java.net.InetAddress;

public class TorChecker {
    File file;
    FileReader fileReader;
    BufferedReader br;

    public TorChecker(){

        try {
            this.file = new File("tmp/exitnodes.txt");
            this.fileReader = new FileReader(file);
            this.br = new BufferedReader(fileReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean checkNodeList(String ip_address) {
        try {
            InetAddress ipInet = InetAddress.getByName(ip_address);
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                InetAddress lineInet = InetAddress.getByName(line);

                boolean flag = lineInet.equals(ipInet);
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
}