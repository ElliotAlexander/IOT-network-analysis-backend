package soton.gdp31.utils.NetworkUtils;

import soton.gdp31.enums.ScanType;
import soton.gdp31.logger.Logging;

import java.io.IOException;
import java.net.*;
import java.util.*;

/**
 * The class scans ports for a given address within the given configuration declared.
 * 
 * @author Elliott Power
 */

 public class PortScanner {


    /**
     * Variable declaration
     */
    private String targetAddress;      // Host to scan
    private int fromPort = 0;   // Port to start range of scanning from
    private int toPort = 65535;     // Port to finish range of scanning to
    private int targetPort = 0; // Target port to be scanned
    
    private ScanType scanMode = RANGE;  // Default is ALL ports will be scanned.

    private List<Integer> portList;

    // Literals for min & max
    private static final int MINIMUM_PORT = 0;
    private static final int MAXIMUM_PORT = 65535;

    // Literals for types of scanning
    private static final ScanType RANGE = ScanType.RANGE;
    private static final ScanType SINGLE = ScanType.SINGLE;
    private static final ScanType ALL = ScanType.ALL;

    
    /**
     * Constructors
     */
    public PortScanner(ScanType scanMode, int targetPort, int fromPort, int toPort, String targetAddress) {
        // Validators
        if (targetPort < MINIMUM_PORT || targetPort > MAXIMUM_PORT) {
            Logging.logErrorMessage("PORTSCANNER: Target port was out of bounds during initalisation. Must be between 0 and 65535.");
            Logging.logErrorMessage("PORTSCANNER: Port passed to initalisation: " + targetPort);
            
            targetPort = MINIMUM_PORT;
            Logging.logErrorMessage("PORTSCANNER: Target port has been set to: " + MINIMUM_PORT);
        }

        if (fromPort < MINIMUM_PORT || fromPort > MAXIMUM_PORT) {
            Logging.logErrorMessage("PORTSCANNER: Start of port scan range was out of bounds during initalisation. Must be higher than 0.");
            Logging.logErrorMessage("PORTSCANNER: Start Range passed to initalisation: " + fromPort);
            
            fromPort = MINIMUM_PORT;
            Logging.logErrorMessage("PORTSCANNER: Start Range has been set to: " + MINIMUM_PORT);
        }

        if (toPort < MINIMUM_PORT || toPort > MAXIMUM_PORT) {
            Logging.logErrorMessage("PORTSCANNER: Start of port scan range was out of bounds during initalisation. Must be lower than 65535.");
            Logging.logErrorMessage("PORTSCANNER: Start Range passed to initalisation: " + fromPort);
            
            fromPort = MAXIMUM_PORT;
            Logging.logErrorMessage("PORTSCANNER: Start Range has been set to: " + MINIMUM_PORT);
        }

        if (scanMode == RANGE && fromPort > toPort) {
            Logging.logErrorMessage("PORTSCANNER: Range of ports to scan invalid. FromPort must be higher than 0, ToPort must be lower than 65535.");
            Logging.logErrorMessage("PORTSCANNER: Start Range passed to initalisation: " + fromPort);
            Logging.logErrorMessage("PORTSCANNER: End Range passed to initalisation: " + toPort);
            
            fromPort = MAXIMUM_PORT;
            toPort = MINIMUM_PORT;
            Logging.logErrorMessage("PORTSCANNER: Start Range has been set to: " + MINIMUM_PORT);
            Logging.logErrorMessage("PORTSCANNER: End Range has been set to: " + MAXIMUM_PORT);
        }

        this.targetPort = targetPort;
        this.fromPort = fromPort;
        this.toPort = toPort;
        this.targetAddress = targetAddress;
        this.scanMode = scanMode;
    }

    public PortScanner(int targetPort, String targetAddr) {
        // Single port scan on a target address.
        this(SINGLE, targetPort, 0, 0, targetAddr);
    }

    public PortScanner(int fromPort, int toPort, String targetAddr) {
        // Port scan on all ports between range on a target address.
        this(RANGE, 0, fromPort, toPort, targetAddr);
    }


    /**
     * Getters and setters
     */
    public static boolean between(int i, int minValueInclusive, int maxValueInclusive) {
        return (i >= minValueInclusive && i <= maxValueInclusive);
    }

    public ScanType getScanType() {
        return this.scanMode;
    }

    public void setScanType(ScanType scanMode) {
        this.scanMode = scanMode;
    }

    public int getTargetPort() {
        return this.targetPort;
    }

    public void setTargetPort(int targetPort) {
        if (between(targetPort, MINIMUM_PORT, MAXIMUM_PORT)){
            this.targetPort = targetPort;
        } else {
            Logging.logErrorMessage("PORTSCANNER: setTargetPort called with a port number out of range.");
        }
    }

    public int getFromPort() {
        return this.fromPort;
    }

    public void setFromPort(int fromPort){
        if (between(fromPort, MINIMUM_PORT, MAXIMUM_PORT)){
            this.fromPort = fromPort;
        } else {
            Logging.logErrorMessage("PORTSCANNER: setFromPort called with a port number out of range.");
        }
    }

    public int getToPort() {
        return this.toPort;
    }

    public void setToPort(int toPort){
        if (between(toPort, MINIMUM_PORT, MAXIMUM_PORT)){
            this.toPort = targetPort;
        } else {
            Logging.logErrorMessage("PORTSCANNER: setToPort called with a port number out of range.");
        }
    }

    public String getTargetAddress() {
        return this.targetAddress;
    }

    public void setTargetAddress(String targetAddr) {
        // TODO: How to error check this?
        this.targetAddress = targetAddr;
    }

    public List<Integer> getPortList() {
        return this.portList;
    }

    public void setPortList(List<Integer> portList){
        this.portList = portList;
    }


    /**
     *  Primary scanning function.
     */

    public ScanOutcome Scan() {
        /**
         * Input: Takes the class configuration.
         * Return: Configured ScanOutcome object
         */

         ScanOutcome outcome = new ScanOutcome();

         switch (scanMode) {
             case RANGE:
                outcome.setAddress(targetAddress);
                for (int i = fromPort; i < toPort; i++) {
                    Boolean resultTCP = ScanTCPSinglePort(targetAddress, i);
                    Boolean resultUDP = ScanUDPSinglePort(targetAddress, i);
                    if (resultTCP) {
                        outcome.addTCPResult(i);
                    }
                    if (resultUDP) {
                        outcome.addUDPResult(i);
                    }
                }
                break;

            case SINGLE:
                outcome.setAddress(targetAddress);
                Boolean resultTCP = ScanTCPSinglePort(targetAddress, targetPort);
                Boolean resultUDP = ScanUDPSinglePort(targetAddress, targetPort);
                if (resultTCP) {
                    outcome.addTCPResult(targetPort);
                }
                if (resultUDP) {
                    outcome.addUDPResult(targetPort);
                }
                break;
                
            case ALL:
                outcome.setAddress(targetAddress);
                for (int i = MINIMUM_PORT; i < MAXIMUM_PORT; i++) {
                    Boolean result_TCP = ScanTCPSinglePort(targetAddress, i);
                    Boolean result_UDP = ScanUDPSinglePort(targetAddress, i);
                    if (result_TCP) {
                        outcome.addTCPResult(i);
                    }
                    if (result_UDP) {
                        outcome.addUDPResult(i);
                    }
                }
                break;
            default:
                Logging.logErrorMessage("PORTSCANNER: Incorrect scan range input.");
         }

         return outcome;
    }


    /**
     * Scans a single port, helper function for the scan function above.
     * 
     * @param targetAddress
     * @param targetPort
     * @return 
     */
    private Boolean ScanTCPSinglePort(String targetAddress, int targetPort) {
        ServerSocket socket = null;             // TCP check        // Check with Elliot - do i want to be using ServerSocket or Socket to encapsulate which side of comms with the device?
        InetAddress address = null;             // target object

        try {
            address = InetAddress.getByName(targetAddress);

            socket = new ServerSocket(targetPort, 2, address);
            socket.setReuseAddress(true); // Ignores a previous connection in a timeout state
            
            if (socket != null) { // Connection enabled, TCP service on this port.
                socket.close();
            }
            // Can always return true if code reaches this point.
            // Socket will throw exception on a rejected connection and in caught claus.
            return true;
        } catch (IOException e) {
            // Connection refused.
        }

        // If this code is reached, both have thrown IOExceptions
        // Connection refused by UDP and TCP. No active service on port.
        return false;
    }

    private Boolean ScanUDPSinglePort(String targetAddress, int targetPort) {
        DatagramSocket datagramSocket = null;   // UDP check
        InetAddress address = null;             // target object

        try {
            address = InetAddress.getByName(targetAddress);

            datagramSocket = new DatagramSocket(targetPort, address);
            datagramSocket.setReuseAddress(true);

            if (datagramSocket != null) { // Connection enabled, UDP service on this port.
                datagramSocket.close();
            }
            // Always return true as above.
            return true;
        } catch (IOException e) {
            // Connection refused.
        }

        // If this code is reached, both have thrown IOExceptions
        // Connection refused by UDP and TCP. No active service on port.
        return false;
    }
 }