package main.java.soton.gdp31.rating;

public class DeviceRating {
    private byte[] uuid; // Identifier

    // Ratings.
    //private double ports_rating;
    private double location_rating;
    private double protocol_rating;
    private double tor_rating;

    // Overall Rating
    private double overall_rating; // DONE



    // Metrics for ports
    private int[] open_tcp_ports;
    private int[] all_port_traffic;
    private double open_ports_normalized; // DONE
    private double traffic_ports_normalized;

    // Metrics for location.
    private boolean russian_traffic;
    private boolean south_pacific_traffic;
    private boolean chinese_traffic;
    private boolean brazil_traffic;

    // Metrics for protocol.
    private int packet_count_for_device;
    private double max_packet_size;
    private double min_packet_size;
    private double average_packet_size;

    private boolean https_only;
    private double https_normalized; // DONE

    private double data_in;
    private double data_out;

    private double upload_normalized; // DONE

    // Metrics from dns.
    private int dns_query_volume;

    // Metrics for tor.
    private int tor_nodes_contacted;


    public DeviceRating(){

    }

    // Getters & Setters.
    public byte[] getUuid() {
        return uuid;
    }

    public void setUuid(byte[] uuid) {
        this.uuid = uuid;
    }
    /*
    public double getPorts_rating() {
        return ports_rating;
    }

    public void setPorts_rating(double ports_rating) {
        this.ports_rating = ports_rating;
    }


     */

    public int[] getAll_port_traffic() {
        return all_port_traffic;
    }

    public void setAll_port_traffic(int[] all_port_traffic) {
        this.all_port_traffic = all_port_traffic;
    }

    public double getLocation_rating() {
        return location_rating;
    }

    public double getOpen_ports_normalized() {
        return open_ports_normalized;
    }

    public void setOpen_ports_normalized(double open_ports_normalized) {
        this.open_ports_normalized = open_ports_normalized;
    }

    public void setLocation_rating(double location_rating) {
        this.location_rating = location_rating;
    }

    public double getProtocol_rating() {
        return protocol_rating;
    }

    public void setProtocol_rating(double protocol_rating) {
        this.protocol_rating = protocol_rating;
    }

    public double getTor_rating() {
        return tor_rating;
    }

    public void setTor_rating(double tor_rating) {
        this.tor_rating = tor_rating;
    }

    public double getOverall_rating() {
        return overall_rating;
    }

    public void setOverall_rating(double overall_rating) {
        this.overall_rating = overall_rating;
    }

    public int[] getOpen_tcp_ports() {
        return open_tcp_ports;
    }

    public void setOpen_tcp_ports(int[] open_tcp_ports) {
        this.open_tcp_ports = open_tcp_ports;
    }

    public boolean isRussian_traffic() {
        return russian_traffic;
    }

    public void setRussian_traffic(boolean russian_traffic) {
        this.russian_traffic = russian_traffic;
    }

    public boolean isSouth_pacific_traffic() {
        return south_pacific_traffic;
    }

    public void setSouth_pacific_traffic(boolean south_pacific_traffic) {
        this.south_pacific_traffic = south_pacific_traffic;
    }

    public boolean isChinese_traffic() {
        return chinese_traffic;
    }

    public void setChinese_traffic(boolean chinese_traffic) {
        this.chinese_traffic = chinese_traffic;
    }

    public boolean isBrazil_traffic() {
        return brazil_traffic;
    }

    public void setBrazil_traffic(boolean brazil_traffic) {
        this.brazil_traffic = brazil_traffic;
    }

    public int getPacket_count_for_device() {
        return packet_count_for_device;
    }

    public void setPacket_count_for_device(int packet_count_for_device) {
        this.packet_count_for_device = packet_count_for_device;
    }

    public double getMax_packet_size() {
        return max_packet_size;
    }

    public void setMax_packet_size(double max_packet_size) {
        this.max_packet_size = max_packet_size;
    }

    public double getMin_packet_size() {
        return min_packet_size;
    }

    public void setMin_packet_size(double min_packet_size) {
        this.min_packet_size = min_packet_size;
    }

    public double getAverage_packet_size() {
        return average_packet_size;
    }

    public void setAverage_packet_size(double average_packet_size) {
        this.average_packet_size = average_packet_size;
    }

    public boolean isHttps_only() {
        return https_only;
    }

    public void setHttps_only(boolean https_only) {
        this.https_only = https_only;
    }

    public double getData_in() {
        return data_in;
    }

    public void setData_in(double data_in) {
        this.data_in = data_in;
    }

    public double getData_out() {
        return data_out;
    }

    public void setData_out(double data_out) {
        this.data_out = data_out;
    }

    public double getUpload_normalized() {
        return upload_normalized;
    }

    public void setUpload_normalized(double upload_normalized) {
        this.upload_normalized = upload_normalized;
    }

    public int getDns_query_volume() {
        return dns_query_volume;
    }

    public void setDns_query_volume(int dns_query_volume) {
        this.dns_query_volume = dns_query_volume;
    }

    public int getTor_nodes_contacted() {
        return tor_nodes_contacted;
    }

    public void setTor_nodes_contacted(int tor_nodes_contacted) {
        this.tor_nodes_contacted = tor_nodes_contacted;
    }

    public double getHttps_normalized() {
        return https_normalized;
    }

    public void setHttps_normalized(double https_normalized) {
        this.https_normalized = https_normalized;
    }

    public double getTraffic_ports_normalized() {
        return traffic_ports_normalized;
    }

    public void setTraffic_ports_normalized(double traffic_ports_normalized) {
        this.traffic_ports_normalized = traffic_ports_normalized;
    }

}
