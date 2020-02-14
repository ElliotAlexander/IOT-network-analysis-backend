package soton.gdp31.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class UUIDGenerator {
    public static byte[] generateUUID(String mac_address) throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("MD5").digest(mac_address.getBytes());
    }
}
