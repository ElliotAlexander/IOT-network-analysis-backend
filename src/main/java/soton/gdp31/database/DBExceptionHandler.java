package soton.gdp31.database;

import soton.gdp31.logger.Logging;

public class DBExceptionHandler {

    public DBExceptionHandler(Exception e, soton.gdp31.database.DBConnection c){
        if(c.connection_down){
            // Ignore the error until the database wrapper gives up reconnecting.
            return;
        } else {
            Logging.logErrorMessage("Unhandled database exception.");
            e.printStackTrace();
        }
    }
}
