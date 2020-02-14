package soton.gdp31.exceptions.database;

public class DBConnectionClosedException extends Exception {

    public DBConnectionClosedException(){
        super("Database connection closed.");
    }
}
