package soton.gdp31.database;

import soton.gdp31.logger.Logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private Connection connection;

    public DBConnection() {
        try{
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/postgres", "postgres", "tallest-tree");
            Logging.logInfoMessage("Database connected.");
            new DatabaseMonitor().run();
        } catch(ClassNotFoundException e1){
            Logging.logErrorMessage("Database connection failed. Stack Trace:");
            e1.printStackTrace();
        } catch (SQLException e){
            Logging.logErrorMessage("Failed to connect to database.");
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
                throw new RuntimeException();
            } catch (InterruptedException e){
                throw new RuntimeException();
            }
        }
    }

}
