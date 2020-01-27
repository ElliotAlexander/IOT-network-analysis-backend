package soton.gdp31.threads;

import soton.gdp31.database.DBConnection;
import soton.gdp31.database.DBDeviceHandler;
import soton.gdp31.logger.Logging;
import soton.gdp31.manager.DeviceListManager;
import soton.gdp31.wrappers.DeviceWrapper;

public class DeviceMonitorThread extends Thread {

    private final DeviceListManager manager;
    private final DBDeviceHandler handler;

    public DeviceMonitorThread(DeviceListManager manager, DBDeviceHandler handler) {
        this.manager = manager;
        this.handler = handler;
        this.start();
    }


    @Override
    public void run(){
        long current = System.currentTimeMillis();
        long data_transferred = 0, data_in=0, data_out=0;
        while(true){
            if((System.currentTimeMillis() - current) > 10000){
                data_transferred = 0;
                data_in = 0;
                data_out = 0;
                for(DeviceWrapper deviceWrapper : manager.getDevices()){
                     data_transferred += deviceWrapper.getDataTransferred();
                     data_in += deviceWrapper.getDataIn();
                     data_out += deviceWrapper.getDataOut();
                }
                handler.updateSumDeviceStats(data_transferred, data_in, data_out);
                Logging.logInfoMessage("Updated cumulative device stats.");
                current = System.currentTimeMillis();
            }
        }
    }
}
