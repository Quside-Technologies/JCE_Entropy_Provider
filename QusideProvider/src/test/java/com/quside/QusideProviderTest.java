package test.java.com.quside;
import com.quside.QusideProvider;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;

import static org.junit.jupiter.api.Assertions.*;



public class QusideProviderTest {

    @Test
    public void testSecureRandomWithQusideProvider_QES() {
        try {

            if (Security.getProvider("Quside") == null) {
                Security.addProvider(new QusideProvider());
            }

            SecureRandom secureRandom = SecureRandom.getInstance("QES", "Quside");

            byte[] randomBytes = new byte[128];
            secureRandom.nextBytes(randomBytes);

            System.out.println(Arrays.toString(randomBytes));

            assertNotNull(randomBytes);
            assertEquals(128, randomBytes.length);

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            fail("Exception occurred: " + e.getMessage());
        } finally {
            Security.removeProvider("Quside");
        }
    }
    
    @Test
    public void testSecureRandomWithQusideProvider_QRNG() {
        try {

            if (Security.getProvider("Quside") == null) {
                Security.addProvider(new QusideProvider());
            }

            SecureRandom secureRandom = SecureRandom.getInstance("QRNG", "Quside");

            byte[] randomBytes = new byte[128];
            secureRandom.nextBytes(randomBytes);

            System.out.println(Arrays.toString(randomBytes));

            assertNotNull(randomBytes);
            assertEquals(128, randomBytes.length);
 

        } catch (Exception e) {
            fail("Exception occurred: " + e.getMessage());
        } finally {
            Security.removeProvider("Quside");
        }
    }
    
    @Test
    public void testSecureRandomWithSunProvider() {
        try {
            SecureRandom secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");

            byte[] randomBytes = new byte[128];
            secureRandom.nextBytes(randomBytes);

            assertNotNull(randomBytes);
            assertEquals(128, randomBytes.length);

        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            fail("Exception occurred: " + e.getMessage());
        } 
    }
}