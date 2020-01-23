package soton.gdp31.threads;

import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBLocationHandler;

public class LocationProcessingThread extends Thread {

    private DBConnection connection_handler;
    private DBLocationHandler location_database_handler;

}