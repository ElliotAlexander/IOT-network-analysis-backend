package main.java.soton.gdp31.utils.TorChecker;

import java.io.*;

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
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
                boolean flag = line.toLowerCase().contains(ip_address.toLowerCase());

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