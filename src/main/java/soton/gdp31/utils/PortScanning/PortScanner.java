package main.java.soton.gdp31.utils.PortScanning;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;

public class PortScanner {

    public Future<PortScanResult> is_tcp_port_open(final ExecutorService es, final String ip, final int port, final int timeout){
        return es.submit(new Callable<PortScanResult>() {
            @Override
            public PortScanResult call(){
                try {
                    Socket socket = new Socket();

                    socket.connect(new InetSocketAddress(ip, port), timeout);
                    socket.close();
                    return new PortScanResult(ip, port, true, true);
                } catch (IOException e) {
                    return new PortScanResult(ip, port, false, true);
                }
            }
        });
    }

    public ArrayList<PortScanResult> scan_device_ports(String ip){
        final ExecutorService es = Executors.newFixedThreadPool(20);
        final int timeout = 200;
        final List<Future<PortScanResult>> futures = new ArrayList<>();

        soton.gdp31.logger.Logging.logInfoMessage("Starting scan .... on ... " + ip);
        for(int port = 1; port <= 65535; port++){
            futures.add(is_tcp_port_open(es, ip, port, timeout));
        }
        es.shutdown();
        soton.gdp31.logger.Logging.logInfoMessage("Finished scan");
        ArrayList<PortScanResult> results = new ArrayList<>();

        try{
            for (final Future<PortScanResult> f : futures) {
                results.add(f.get());
            }

        } catch (InterruptedException e) {
            soton.gdp31.logger.Logging.logWarnMessage("Interrupted Exception error thrown when attempting to scan device: " + ip);
            e.printStackTrace();
        } catch (ExecutionException e) {
            soton.gdp31.logger.Logging.logWarnMessage("Execution Exception error thrown when attempting to scan device: " + ip);
            e.printStackTrace();
        }

        return results;
    }

    public String extract_list_of_open_ports(ArrayList<PortScanResult> results){
        Iterator i = results.iterator();
        ArrayList<Integer> list_of_ints = new ArrayList<>();
        while(i.hasNext()){
            PortScanResult scanResult = (PortScanResult) i.next();
            int portNumber = scanResult.getPort();
            list_of_ints.add(portNumber);
        }

        StringBuilder sb = new StringBuilder();
        for(int j = list_of_ints.size() - 1; j >= 0; j--){
            int num = list_of_ints.get(j);
            sb.append(num);
            sb.append(',');
        }

        // Remove last ','
        String result = sb.toString();
        result = result.substring(0, result.length()-1);

        return result;
    }
}
