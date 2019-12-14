package soton.gdp31.utils.NetworkUtils;

import java.lang.reflect.Array;
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
    private byte[] uuid;
    private List<Integer> tcp_ports; // List of port numbers, where an open TCP service is.
    private List<Integer> udp_ports; // List of port numbers, where an open UDP service is.
    private String targetAddress;
    private long timeOfScan; // TODO: Add implementation to get time during scan.

    public ScanOutcome() {
        targetAddress = "";
    }

    public ScanOutcome(byte[] uuid) {
        this.uuid = uuid;
        targetAddress = "";
    }

    /**
     * Getters & Setters
     */
    public void setUUID(byte[] uuid) {
        this.uuid = uuid;
    }

    public byte[] getUUID(){
        return uuid;
    }
    
    public void setTCPPorts(List<Integer> ports) {
        this.tcp_ports = ports;
    }

    public List<Integer> getTCPPorts(){
        return tcp_ports;
    }

    public void setUDPPorts(List<Integer> ports) {
        this.udp_ports = ports;
    }

    public List<Integer> getUDPPorts() {
        return udp_ports;
    }

    public void setAddress(String targetAddr) {
        this.targetAddress = targetAddr;
    }

    public String getAddress() {
        return targetAddress;
    }

    public void setTimeOfScan(long timeOfScan) {
        this.timeOfScan = timeOfScan;
    }

    public long getTimeOfScan() {
        return timeOfScan;
    }

    /**
     * Helpers
     */
    public void addTCPResult(Integer port) {
        tcp_ports.add(port);
    }

    public void addUDPResult(Integer port) {
        udp_ports.add(port);
    }

    public void clearAllResults() {
        tcp_ports.clear();
        udp_ports.clear();
    }

    public void clearTCPResults() {
        tcp_ports.clear();
    }

    public void clearUDPResults() {
        udp_ports.clear();
    }
}