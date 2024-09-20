import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;

public class QusideQrngCheck {
    public static void main(String[] args) {
        // Retrieve the list of strong algorithms from java.security properties
        String strongAlgorithms = Security.getProperty("securerandom.strongAlgorithms");
        if (strongAlgorithms == null || strongAlgorithms.isEmpty()) {
            System.err.println("No strong algorithms defined in java.security");
            return;
        }

        // Parse the strong algorithms
        String[] strongAlgEntries = strongAlgorithms.split("\\s*,\\s*");
        Set<String> strongAlgorithmSet = new HashSet<>();

        System.out.println("SecureRandom.getInstanceStrong() Check");

	try
	{
	    SecureRandom random = SecureRandom.getInstanceStrong();

	    System.out.print("Algorithm: " + random.getAlgorithm());
	    System.out.println(", Provider: " + random.getProvider().getName());
	    if ("QRNG".equalsIgnoreCase(random.getAlgorithm())
	    && "Quside".equalsIgnoreCase(random.getProvider().getName()))
	    {
	       System.out.println("Quside QRNG entropy provider installed correctly");
	    }
	    else
	    {
	       System.out.println("Quside QRNG entropy provider not installed");
	    }
	}
	catch (NoSuchAlgorithmException e)
	{
	    System.out.println("getInstanceStrong() not properly configured:");
	    e.printStackTrace(System.out);
	}

	System.out.println("*************************");
	System.out.println("BC Sample Test:");
	try
	{

                  Security.addProvider(new BouncyCastleFipsProvider());

	    SecureRandom bcRandom = SecureRandom.getInstance("DEFAULT", "BCFIPS");

	    System.err.println(Hex.toHexString(bcRandom.generateSeed(32)));
	}
	catch (NoSuchAlgorithmException | NoSuchProviderException e)
	{
		e.printStackTrace();
	}

    }
}


