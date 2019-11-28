package soton.gdp31.utils.NetworkUtils;

import java.util.*;

public class ScanOutcome {
    /**
     * Returning data object of the results of a PortScan.
     * 
     * @author Elliott Power
     */

    /**
     * Variable initalisation
     */
    private Map<Integer, Boolean> ports; // Port number, Active Service
    private String targetAddress;

    public ScanOutcome() {
        ports = new HashMap<Integer, Boolean>();
        targetAddress = "";
    }

    /**
     * Getters & Setters
     */
    public void setPorts(Map<Integer, Boolean> ports) {
        this.ports = ports;
    }

    public Map<Integer, Boolean> getPorts(){
        return ports;
    }

    public void setAddress(String targetAddr) {
        this.targetAddress = targetAddr;
    }

    public String getAddress() {
        return targetAddress;
    }

    /**
     * Helpers
     */
    public void addResult(Integer port, Boolean open) {
        ports.put(port, open);
    }

    public Boolean getResult(Integer port){
        return ports.get(port);
    }

    public void clearResults() {
        ports.clear();
    }
}