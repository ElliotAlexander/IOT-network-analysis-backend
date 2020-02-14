package soton.gdp31.exceptions;

public class InterfaceUnknownException extends Exception{

    public InterfaceUnknownException(String interface_name){
        super("Failed to open interface " + interface_name);
    }
}
