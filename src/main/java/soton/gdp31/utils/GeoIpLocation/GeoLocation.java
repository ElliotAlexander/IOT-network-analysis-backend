package soton.gdp31.utils.GeoIpLocation;

import com.maxmind.geoip.Location;

public class GeoLocation {

    private String countryCode;
    private String countryName;
    private String postalCode;
    private String city;
    private String region;
    private int areaCode;
    private int dmaCode;
    private int metroCode;
    private float latitude;
    private float longitude;

    private Long last_scanned;
    private String hostname;

    public GeoLocation(String countryCode, String countryName, String postalCode, String city, String region,
                       int areaCode, int dmaCode, int metroCode, float latitude, float longitude, Long last_scanned, String hostname) {
        this.countryCode = countryCode;
        this.countryName = countryName;
        this.postalCode = postalCode;
        this.city = city;
        this.region = region;
        this.areaCode = areaCode;
        this.dmaCode = dmaCode;
        this.metroCode = metroCode;
        this.latitude = latitude;
        this.longitude = longitude;
        this.last_scanned = last_scanned;
        this.hostname = hostname;
    }

    public static GeoLocation map(Location loc, Long last_scanned, String hostname){
        return new GeoLocation(loc.countryCode, loc.countryName, loc.postalCode, loc.city, loc.region,
                loc.area_code, loc.dma_code, loc.metro_code, loc.latitude, loc.longitude, last_scanned, hostname);
    }

    public GeoLocation(float latitude, float longitude, Long last_scanned, String hostname){
        this.latitude = latitude;
        this.longitude = longitude;
        this.last_scanned = last_scanned;
        this.hostname = hostname;
    }

    @Override
    public String toString() {
        return "GeoLocation{" +
                "countryCode='" + countryCode + '\'' +
                ", countryName='" + countryName + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", city='" + city + '\'' +
                ", region='" + region + '\'' +
                ", areaCode=" + areaCode +
                ", dmaCode=" + dmaCode +
                ", metroCode=" + metroCode +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", last_scanned=" + last_scanned +
                ", hostname=" + hostname +
                '}';
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public int getAreaCode() {
        return areaCode;
    }

    public int getDmaCode() {
        return dmaCode;
    }

    public int getMetroCode() {
        return metroCode;
    }

    public String getCity() {
        return city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getRegion() {
        return region;
    }

    public void setLast_scanned(Long last_scanned) {
        this.last_scanned = last_scanned;
    }

    public Long getLast_scanned(){
        return last_scanned;
    }

    public String getHostname(){
        return hostname;
    }

    public void setHostname(String hostname){
        this.hostname = hostname;
    }
}