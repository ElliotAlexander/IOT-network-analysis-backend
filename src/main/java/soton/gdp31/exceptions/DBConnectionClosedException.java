package soton.gdp31.exceptions;

public class DBConnectionClosedException extends Exception {

    public DBConnectionClosedException(){
        super("Database connection closed.");
    }
}
