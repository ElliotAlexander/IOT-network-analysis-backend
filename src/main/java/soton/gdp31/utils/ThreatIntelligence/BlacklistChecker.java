package soton.gdp31.utils.ThreatIntelligence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import soton.gdp31.logger.Logging;

public class BlacklistChecker {

    // Provided by nothink.org   
    // Telnet blacklist, 2019 (IP address)
    // Generated 2019-12-15 23:00:06 UTC
    
    final String blacklist_snmp = "src/main/java/soton/gdp31/utils/ThreatIntelligence/blacklist_snmp.txt";
    final String blacklist_ssh = "src/main/java/soton/gdp31/utils/ThreatIntelligence/blacklist_ssh.txt";
    final String blacklist_telnet = "src/main/java/soton/gdp31/utils/ThreatIntelligence/blacklist_telnet.txt";

    public BlacklistChecker() {
        File snmp = new File(blacklist_snmp);
        File ssh = new File(blacklist_ssh);
        File telnet = new File(blacklist_telnet);

        FileReader fr_snmp;
        FileReader fr_ssh;
        FileReader fr_telnet;

        try {
            fr_snmp = new FileReader(snmp);
            fr_ssh = new FileReader(ssh);
            fr_telnet = new FileReader(telnet);
        } catch () {
            Logging.logErrorMessage("FileReader not worked.");
        }

        BufferedReader br_snmp = new BufferedReader(fr_snmp);
        BufferedReader br_ssh = new BufferedReader(fr_ssh);
        BufferedReader br_telnet = new BufferedReader(fr_telnet);

    }
    public Boolean checkAddress(String addr) {
        String line;
        while((line = br_snmp.readLine()) != null){
            // process the line
            if(addr.equals(line)) {
                return true;
            }
        }
        while((line = br_ssh.readLine()) != null){
            if(addr.equals(line)) {
                return true;
            }
        }
        while((line = br_telnet.readLine()) != null){
            if(addr.equals(line)) {
                return true;
            }
        }
        return false;
    }
}