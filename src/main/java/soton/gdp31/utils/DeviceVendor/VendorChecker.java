package soton.gdp31.utils.DeviceVendor;

import soton.gdp31.logger.Logging;

import java.io.*;
import java.util.regex.Pattern;

public class VendorChecker {
    File file;
    FileReader fileReader;
    BufferedReader br;

    public VendorChecker(){

        try {
            this.file = new File("tmp/oui.txt");
            this.fileReader = new FileReader(file);
            this.br = new BufferedReader(fileReader);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public String checkMac(String associated_mac_address) {
        if(associated_mac_address != null){
            try {
                String oui = associated_mac_address.substring(0, 8);
                String line;
                Pattern pattern = Pattern.compile("\\*");
                while ((line = br.readLine()) != null) {
                    // process the line.
                    boolean flag = line.toLowerCase().contains(oui.toLowerCase());

                    if (flag) {
                        String[] data = pattern.split(line);
                        String vendor = data[1];
                        Logging.logInfoMessage("Vendor: " + vendor);
                        return vendor;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
                Logging.logInfoMessage("Vendor not found");
                return null;
            }
            Logging.logWarnMessage("Vendor not found.");
            return null;
        } else {
            Logging.logWarnMessage("Vendor not found: Mac address too short. Mac Address:" + associated_mac_address);
            return null;
        }
    }
}