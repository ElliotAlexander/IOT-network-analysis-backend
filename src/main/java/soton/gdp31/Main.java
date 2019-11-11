package soton.gdp31;

import org.pcap4j.core.*;

import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.exceptions.InterfaceUnknownException;
import soton.gdp31.logger.Logging;
import soton.gdp31.threads.PacketThreadListener;
import soton.gdp31.utils.InterfaceUtils;

public class Main {


    public static final int PCAP_ERROR_EXIT = 4;
    public static final int UNKNOWN_INTERFACE_EXCEPTION_EXIT = 5;
    public static final int DATABASE_CONNECTION_LOST_EXIT = 6;

    private final String interface_name = "en0";
    private final String handle_dump_name = "out.pcap";

    private PcapHandle handle;
    private PcapDumper dumper;

    private PacketThreadListener thread;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {

        // Setup PCAP interface, file dump, database connection and monitoring thread.

        try {
            this.handle = InterfaceUtils.openInterface(interface_name);
            this.dumper = handle.dumpOpen(this.handle_dump_name);
            new DBConnection();
        } catch (PcapNativeException e) {
            e.printStackTrace();
            System.exit(PCAP_ERROR_EXIT);
        } catch (InterfaceUnknownException e) {
            e.printStackTrace();
            System.exit(UNKNOWN_INTERFACE_EXCEPTION_EXIT);
        } catch (NotOpenException e) {
            Logging.logErrorMessage("Failed to open handle.");
            e.printStackTrace();
        } catch (DBConnectionClosedException e){
            System.exit(DATABASE_CONNECTION_LOST_EXIT);
        }

        // Start our listening thread.
        Logging.logInfoMessage("Starting packet listner thread");
        this.thread = new PacketThreadListener(handle, dumper);
        this.thread.start();
    }
}
