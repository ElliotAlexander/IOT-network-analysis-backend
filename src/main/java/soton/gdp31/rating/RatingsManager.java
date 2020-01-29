package soton.gdp31.rating;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.OptionalDouble;
import java.util.stream.DoubleStream;

import soton.gdp31.database.DBRatingHandler;
import soton.gdp31.exceptions.database.DBConnectionClosedException;
import soton.gdp31.wrappers.DeviceWrapper;
import soton.gdp31.database.DBConnection;
import soton.gdp31.manager.DeviceListManager;

public class RatingsManager extends Thread{

    private Connection c;
    private DBRatingHandler rating_handler;
    private DeviceListManager manager;

    public  RatingsManager(DBConnection db_connection_wrapper, DeviceListManager device_list_manager){
        // Get list of memory devices from Device List manager.
        this.manager = device_list_manager;
        try {
            this.rating_handler = new DBRatingHandler(db_connection_wrapper);
        } catch (DBConnectionClosedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        while(true){
                manager.getDevices().forEach((dWrapper) -> {
                    if(twentySecondsAgo(dWrapper.getLast_rating_time())){
                        DeviceRating rating = generateRating(dWrapper);
                        rating_handler.addRating(rating);
                        dWrapper.setLast_rating_time(System.currentTimeMillis());
                    }
                });

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public DeviceRating generateRating(DeviceWrapper dWrapper){
        // Get all seperate ratings.
        Double http_rating = genHTTPSRating(dWrapper);
        Double port_rating = genPortRating(dWrapper);
        Double upload_rating = genUploadRating(dWrapper);
        Double port_traffic_rating = genPortTrafficRating(dWrapper);

        // Generate overall.
        Double overall = -1.0;

        DoubleStream stream = DoubleStream.of(
                http_rating, port_rating, upload_rating, port_traffic_rating
        );
        OptionalDouble obj = stream.average();
        if(obj.isPresent()){
            overall = obj.getAsDouble();
        }

        // Create DeviceRating object.
        DeviceRating rating = new DeviceRating();

        // Setters for DeviceRating.
        // TODO: Move this to constructor if CBA.
        rating.setHttps_normalized(http_rating);
        rating.setOpen_ports_normalized(port_rating);
        rating.setTraffic_ports_normalized(port_traffic_rating);
        rating.setUpload_normalized(upload_rating);

        rating.setOverall_rating(overall);
        return rating;
    }

    // HTTP PORTS RATING GENERATION.


    private Double genHTTPSRating(DeviceWrapper dWrapper){
        Long packet_count = dWrapper.getPacketCount();
        Long https_packet_count = dWrapper.getHttpsPacketCount();

        Double percentage_of = ((double)https_packet_count/packet_count);

        return percentage_of;
    }

    private Double genPortRating(soton.gdp31.wrappers.DeviceWrapper dWrapper) {
        String open_ports_string = rating_handler.pullOpenPorts(dWrapper);

        String[] strArray = open_ports_string.split(",");

        // Build array list from string taken from database.
        ArrayList<Integer> open_ports = new ArrayList<>();
        for(int i = 0; i < strArray.length; i++){
            open_ports.add(Integer.parseInt(strArray[i]));
        }

        // Build bad ports to have open.
        // From dummies.
        // FTP, SSH, Telnet, SMTP, DNS, HTTP(S) over SSL, POP3, RPC, NetBIOS over TCP, SOCKS, MSQL Server, RDP Server
        int[] really_bad_ports = new int[]{21, 22, 23, 25, 53, 443, 110, 135, 137, 138, 139, 1080, 1433, 3389};

        // From https://www.garykessler.net/library/bad_ports.html
        int[] gary_kessler_ports = new int[]{31, 1170, 1234, 1243, 1981, 2001, 2023, 2989, 3024, 3150, 3700, 4950, 6346, 6400, 6667, 6670, 12345, 12346, 16660, 20034, 20432, 20433, 27374, 27444, 27665, 30100, 31335, 31337, 33270, 33568, 40421, 60008, 65000};

        int really_bad_hits = containsAny(open_ports, really_bad_ports);
        int gary_kessler_hits = containsAny(open_ports, gary_kessler_ports);
        int high_hits = containsHigh(open_ports);


        // Rating.
        Double rating = 0.0;

        if(really_bad_hits > 0) return 1.0;

        if(gary_kessler_hits > 0) return 1.0;

        if(high_hits > 0){
            return 0.5;
        }

        return 0.0;
    }

    private Double genUploadRating(DeviceWrapper dWrapper){
        Long upload = dWrapper.getDataOut();
        Long total = dWrapper.getDataTransferred();

        Double percentage_of = ((double) upload/total);
        return percentage_of;
    }

    private Double genPortTrafficRating(DeviceWrapper dWrapper){
        ArrayList<Integer> portTraffic = dWrapper.getPort_traffic();

        // Build bad ports to have open.
        // From dummies.
        // FTP, SSH, Telnet, SMTP, DNS, HTTP(S) over SSL, POP3, RPC, NetBIOS over TCP, SOCKS, MSQL Server, RDP Server
        int[] really_bad_ports = new int[]{21, 22, 23, 25, 53, 443, 110, 135, 137, 138, 139, 1080, 1433, 3389};

        // From https://www.garykessler.net/library/bad_ports.html
        int[] gary_kessler_ports = new int[]{31, 1170, 1234, 1243, 1981, 2001, 2023, 2989, 3024, 3150, 3700, 4950, 6346, 6400, 6667, 6670, 12345, 12346, 16660, 20034, 20432, 20433, 27374, 27444, 27665, 30100, 31335, 31337, 33270, 33568, 40421, 60008, 65000};

        int really_bad_hits = containsAny(portTraffic, really_bad_ports);
        int gary_kessler_hits = containsAny(portTraffic, gary_kessler_ports);
        int high_hits = containsHigh(portTraffic);

        // Rating.
        Double rating = 0.0;

        if(really_bad_hits > 0) return 1.0;

        if(gary_kessler_hits > 0) return 1.0;

        if(high_hits > 0){
            return 0.5;
        }

        return 0.0;

    }
    private int containsHigh(ArrayList<Integer> given_ports){
        int hits = 0;
        for(Integer port : given_ports){
            if(port > 1000){
                hits++;
            }
        }
        return hits;
    }
    private int containsAny(ArrayList<Integer> given_ports, int[] int_array){
        int hits = 0;

        for (Integer port : given_ports){
            // Check really bad.
            if(contains(int_array, port)){
                hits++;
            }
        }
        return hits;
    }

    private boolean contains(final int[] arr, final int key) {
        return Arrays.stream(arr).anyMatch(i -> i == key);
    }















    public boolean twentySecondsAgo(Long last_security_rating){
        // Was last_security_rating more than 20 seconds ago?
        return (System.currentTimeMillis() - last_security_rating > 20000);
    }
}
