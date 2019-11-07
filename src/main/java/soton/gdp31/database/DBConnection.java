package soton.gdp31.database;

import soton.gdp31.exceptions.DBConnectionClosedException;
import soton.gdp31.exceptions.DBMonitorDisconnectedException;
import soton.gdp31.logger.Logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private Connection connection;

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

        public DatabaseMonitor(){
            Logging.logInfoMessage("Initialising database monitor.");
        }

        @Override
        public void run() {
            try {
                do {
                    Thread.sleep(1000);
                } while (!connection.isClosed());
            } catch (SQLException e) {
                e.printStackTrace();
                throw new DBMonitorDisconnectedException("Database Monitor failure.");
            } catch (InterruptedException e){
                e.printStackTrace();
                throw new DBMonitorDisconnectedException("Database failure");
            }
        }
    }

}
