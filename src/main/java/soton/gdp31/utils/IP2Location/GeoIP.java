package soton.gdp31.utils.IP2Location;

public class GeoIP {
    private String ipAddress;
    private String city;
    private String latitude;
    private String longitude;

    // constructors, getters and setters

    public GeoIP(String ipAddress, String city, String latitude, String longitude) {
        this.ipAddress = ipAddress;
        this.city = city;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getCity() {
        return city;
    }
    
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLatitude(){
        return latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLongitude() {
        return longitude;
    }
}