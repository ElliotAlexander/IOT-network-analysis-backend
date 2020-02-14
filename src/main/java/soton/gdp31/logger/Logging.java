package soton.gdp31.logger;

public class Logging {

    private static final String log_prefix = "[LOG]";
    private static final String info_prefix = "[INFO]";
    private static final String error_prefix = "[ERROR]";
    private static final String warn_prefix = "[WARN]";

    public static void logMessage(String s){
        System.out.println(Logging.log_prefix + " " + s);
    }

    public static void logInfoMessage(String s){
        System.out.println(Logging.info_prefix + " " + s);
    }

    public static void logErrorMessage(String s){
        System.out.println(Logging.error_prefix + " " + s);
    }

    public static void logWarnMessage(String s){
        System.out.println(Logging.warn_prefix + " " + s);
    }
}
