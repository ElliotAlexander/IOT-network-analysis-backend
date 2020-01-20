package soton.gdp31.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    public static final ConfigLoader instance = new ConfigLoader();

    private ConfigLoader(){};


    public Properties loadConfig(String config_name){
        try (InputStream input = new FileInputStream(config_name)) {
            Properties prop = new Properties();
            prop.load(input);
            return prop;
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

}
