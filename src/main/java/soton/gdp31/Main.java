package soton.gdp31;

import org.pcap4j.core.*;

import soton.gdp31.database.DBConnection;
import soton.gdp31.exceptions.InterfaceUnknownException;
import soton.gdp31.utils.InterfaceUtils;

public class Main {

    private final String interface_name = "en0";
    private final String handle_dump_name = "out.pcap";

    private PcapHandle handle;
    private PcapDumper dumper;

    private PacketThreadListener thread;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        try {
            this.handle = InterfaceUtils.openInterface(interface_name);
            this.dumper = handle.dumpOpen(this.handle_dump_name);
            new DBConnection();
        } catch (PcapNativeException e) {
            e.printStackTrace();
        } catch (InterfaceUnknownException e) {
            e.printStackTrace();
            return;
        } catch (NotOpenException e) {
            e.printStackTrace();
            return;
        }

        this.thread = new PacketThreadListener(handle, dumper);
        this.thread.start();
    }
}
