package soton.gdp31.utils.ThreatIntelligence;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class BlacklistChecker {

    final String blacklist_snmp = "src\main\java\soton\gdp31\utils\ThreatIntelligence\blacklist_snmp.txt";
    final String blacklist_ssh = "src\main\java\soton\gdp31\utils\ThreatIntelligence\blacklist_ssh.txt";
    final String blacklist_telnet = "src\main\java\soton\gdp31\utils\ThreatIntelligence\blacklist_telnet.txt";

    File snmp = new File(blacklist_snmp);
    File ssh = new File(blacklist_ssh);
    File telnet = new File(blacklist_telnet);

    FileReader fr_snmp = new FileReader(snmp);
    FileReader fr_ssh = new FileReader(ssh);
    FileReader fr_telnet = new FileReader(telnet);

    BufferedReader br_snmp = new BufferedReader(fr_snmp);
    BufferedReader br_ssh = new BufferedReader(fr_ssh);
    BufferedReader br_telnet = new BufferedReader(fr_telnet);

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