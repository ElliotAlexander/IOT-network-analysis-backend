package soton.gdp31.exceptions.runtime;

public class DBMonitorDisconnectedException extends RuntimeException {
    public DBMonitorDisconnectedException(){}

    public DBMonitorDisconnectedException(String message){
        super(message);
    }
}
