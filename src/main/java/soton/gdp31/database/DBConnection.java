package soton.gdp31.database;

import soton.gdp31.exceptions.DBConnectionClosedException;
import soton.gdp31.exceptions.DBMonitorDisconnectedException;
import soton.gdp31.logger.Logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBConnection {

    protected Connection connection;

    public DBConnection() throws DBConnectionClosedException {
        try{
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "tallest-tree");
            Logging.logInfoMessage("Database connected.");
            new DatabaseMonitor().run();
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
                        ResultSet rs = connection.createStatement().executeQuery("select 1;");
                        Thread.sleep(POLLING_COOLDOWN_MS);
                        Logging.logInfoMessage("Connected");
                    } catch (SQLException e) {
                        int retries = 0;
                        connection = null;
                        while (connection == null) {
                            Logging.logInfoMessage("Attempting to reconnect to database.");
                            try {
                                Thread.sleep(RETRY_COOLDOWN_MS);
                                Class.forName("org.postgresql.Driver");
                                DBConnection.this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "tallest-tree");
                                Logging.logInfoMessage("Reconnected!");
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
