package soton.gdp31.utils.PortScanning;

public class PortScanResult {
    private final String ip_address;
    private final int port;
    private final boolean status;
    private final boolean isTCP;

    public PortScanResult(String ip_address, int port, boolean status, boolean isTCP){
        this.ip_address = ip_address;
        this.port = port;
        this.status = status;
        this.isTCP = isTCP;
    }

    public int getPort() {
        return port;
    }

    public boolean isOpen() {
        if(status) return true;
        else return false;
    }

    public boolean isClosed() {
        if (!status) return true;
        else return false;
    }

    public boolean isTCP(){
        if(isTCP) return true;
        else return false;
    }

    public boolean isUDP(){
        if(!isTCP) return true;
        else return false;
    }
}
