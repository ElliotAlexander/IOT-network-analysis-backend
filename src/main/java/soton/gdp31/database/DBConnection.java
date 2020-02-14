package soton.gdp31.database;

import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.runtime.DBMonitorDisconnectedException;
import soton.gdp31.logger.Logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {

    protected Connection connection;
    protected boolean connection_down;

    public DBConnection() throws DBConnectionClosedException {
        try{
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "tallest-tree");
            Logging.logInfoMessage("Database connected.");
            this.connection_down = false;
            new DatabaseMonitor().start();
        } catch(ClassNotFoundException e1){
            Logging.logErrorMessage("Database connection failed. ");
            e1.printStackTrace();
            throw new DBConnectionClosedException();
        } catch (SQLException e){
            Logging.logErrorMessage("SQLException while attempting to connect to database.");
            throw new DBConnectionClosedException();
        }
    }

    public Connection getConnection() throws DBConnectionClosedException {
        try {
            // Violently defensive programming.
            if(this.connection.isClosed()){
                Logging.logErrorMessage("Tried to access the database while down.");
                throw new DBConnectionClosedException();
            } else {
                return this.connection;
            }
        } catch(SQLException e){
            Logging.logErrorMessage("Error attempting to access database connection.");
            throw new DBConnectionClosedException();
        }
    }

    public class DatabaseMonitor extends Thread  {

        public static final int RETRY_CAP = 10;
        public static final int RETRY_COOLDOWN_MS = 5000;
        public static final int POLLING_COOLDOWN_MS = 10000;

        public DatabaseMonitor(){
            Logging.logInfoMessage("Initialising database monitor.");
        }

        @Override
        public void run() {

                while(true) {
                    try {
                        // connection.isClosed doesn't appear to work for dropped backends
                        // connection.isValid isn't implemented

                        // Attempt a basic query
                        ResultSet rs = connection.createStatement().executeQuery("select 1;");
                        // If the query fails, it'll throw a SQL exception.

                        // If no exception sleep and carry on.
                        Thread.sleep(POLLING_COOLDOWN_MS);
                    } catch (SQLException e) {
                        int retries = 1;
                        connection = null;
                        connection_down = true;
                        while (connection == null) {
                            Logging.logInfoMessage("Attempting to reconnect to database.");
                            try {
                                Thread.sleep(RETRY_COOLDOWN_MS);
                                Class.forName("org.postgresql.Driver");
                                DBConnection.this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "tallest-tree");
                                Logging.logInfoMessage("Reconnected!");
                                // Reset retries - in case we lose connection again.
                                retries = 1;
                                connection_down = false;
                                break;
                            } catch (ClassNotFoundException | SQLException ex) {
                                Logging.logWarnMessage("Reconnection failed. Retrying in five seconds. " + retries + " / " + RETRY_CAP);
                                retries++;
                                if(retries == RETRY_CAP) {
                                    throw new DBMonitorDisconnectedException("Failed to reconnect after " + retries + " attempts");
                                }
                            } catch(InterruptedException x){
                                break;
                            }
                        }
                    } catch (InterruptedException e ){
                        throw new RuntimeException();
                    }
                }
        }
    }

}
