import org.junit.jupiter.api.Test;
import soton.gdp31.utils.NetworkUtils.NetworkIdentification;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkIdentificationTest {


    @Test
    public void sameSubnetTest(){
        try {
            assert NetworkIdentification.compareIPSubnets(InetAddress.getByName("127.0.0.1").getAddress(), InetAddress.getByName("127.0.0.55").getAddress(), InetAddress.getByName("255.255.255.0").getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void differentSubnetTests(){
        try {
            assert !NetworkIdentification.compareIPSubnets(InetAddress.getByName("128.0.0.1").getAddress(), InetAddress.getByName("127.0.0.55").getAddress(), InetAddress.getByName("255.255.255.0").getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }


    @Test
    public void erroredSubnetTest(){
        try {

            byte[] one = InetAddress.getByName("127.0.0.1").getAddress();
            byte[] two = { 0x55 };
            byte[] combined = new byte[one.length + two.length];

            System.arraycopy(one,0,combined,0         ,one.length);
            System.arraycopy(two,0,combined,one.length,two.length);
            assert !NetworkIdentification.compareIPSubnets( combined, InetAddress.getByName("127.0.0.55").getAddress(), InetAddress.getByName("255.255.255.0").getAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
