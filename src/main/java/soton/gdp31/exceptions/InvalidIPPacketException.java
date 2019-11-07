package soton.gdp31.exceptions;

public class InvalidIPPacketException extends Exception{

    public InvalidIPPacketException(){
        super("Invalid IP packet detected.");
    }
}
