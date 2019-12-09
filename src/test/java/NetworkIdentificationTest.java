import org.junit.jupiter.api.Test;
import soton.gdp31.utils.NetworkUtils.NetworkIdentification;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkIdentificationTest {


    @Test
    public void sameSubnetTest(){
        try {
            NetworkIdentification.compareIPSubnets(InetAddress.getByName("127.0.0.1").getAddress(), InetAddress.getByName("127.0.0.55").getAddress(), InetAddress.getByName("255.255.255.0").getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
