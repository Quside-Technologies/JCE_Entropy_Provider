import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;
import org.bouncycastle.crypto.*;
import org.bouncycastle.crypto.util.*;
import org.bouncycastle.crypto.fips.*;
import java.io.*;
import java.util.Scanner;


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


        System.out.println("\u001B[33m"); //yellow
        System.out.println("SecureRandom.getInstanceStrong() Check");
        System.out.println("\u001B[0m"); //reset colour

	try
	{
	    SecureRandom random = SecureRandom.getInstanceStrong();
              System.out.println("\u001B[32m"); //green
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

        System.out.println("\u001B[33m"); //yellow
	System.out.println("*************************");
	System.out.println("BC FIPS Entropy Generation Test:");
        System.out.println("");
        System.out.println("\u001B[0m"); //reset

	try
	{

            Security.addProvider(new BouncyCastleFipsProvider());

	    SecureRandom bcRandom = SecureRandom.getInstance("DEFAULT", "BCFIPS");
            
            System.out.println("\u001B[32m"); //green
	    System.err.println(Hex.toHexString(bcRandom.generateSeed(32)));
            System.out.println("\u001B[0m"); //reset colour

	}
	
      catch (NoSuchAlgorithmException | NoSuchProviderException e)
	{
		e.printStackTrace();
	}
        System.out.println("\u001B[33m"); //yellow
        System.out.println("");
        System.out.println("*************************");
	System.out.println("BC FIPS Symmetric Key Mechanisms Test:");
        System.out.println("");
        System.out.println("\u001B[0m"); //reset colour

	try
          {
              

	  // ensure a FIPS DRBG in use.
                   CryptoServicesRegistrar.setSecureRandom(FipsDRBG.SHA512_HMAC.fromEntropySource(new BasicEntropySourceProvider(new SecureRandom(), true)).build(null, true));
 
               byte[] iv = new byte[16];

                       CryptoServicesRegistrar.getSecureRandom().nextBytes(iv); 
  
        System.out.println("\u001B[33m"); //yellow
        System.out.println("");
        System.out.println("*************************");
	System.out.println("BC FIPS IV generated:");
        System.out.println("\u001B[32m"); //green 
        System.err.println(Hex.toHexString(iv));
        System.out.println("");
        System.out.println("\u001B[0m"); //red
 
                FipsSymmetricKeyGenerator<SymmetricSecretKey> keyGen = new FipsAES.KeyGenerator(128, CryptoServicesRegistrar.getSecureRandom());
 
                SymmetricSecretKey key = keyGen.generateKey();
        System.out.println("\u001B[33m"); //yellow
        System.out.println("*************************");
	System.out.println("BC FIPS Encoded Symmetric Key");
        System.out.println("\u001B[32m"); //green
        System.err.println(key);
        System.out.println("");
        System.out.println("");
       System.out.println("\u001B[0m"); //reset
 
           FipsSymmetricOperatorFactory<FipsAES.Parameters> fipsSymmetricFactory = new FipsAES.OperatorFactory();
            
           FipsOutputEncryptor<FipsAES.Parameters> outputEncryptor = fipsSymmetricFactory.createOutputEncryptor(key,FipsAES.CBCwithPKCS7.withIV(iv));
 
         

//get soem inoput to encrypt

            Scanner myObj = new Scanner(System.in);
               System.out.println("\u001B[33m"); //yellow
               System.out.println("Enter text to encrypt:");
               System.out.println("\u001B[0m"); //reset

    // String input
             String txttoencrypt = myObj.nextLine();

   

    // Output input by user
         System.out.println("\u001B[32m"); //green
         System.out.println("Your Input: " + txttoencrypt);

        byte[] textbyte = txttoencrypt.getBytes();
  
        byte[] output = encryptBytes(outputEncryptor, textbyte);

         System.out.println("\u001B[33m"); //yellow
       System.out.println("*************************");
      
       System.out.println("BC FIPS Encoded Text");
      System.out.println("\u001B[32m"); //green
       System.err.println(Hex.toHexString(output));
       System.out.println("\u001B[0m"); //reset colour
       System.out.println("");
       System.out.println("");

     

 
           FipsInputDecryptor<FipsAES.Parameters> inputDecryptor =fipsSymmetricFactory.createInputDecryptor(key,FipsAES.CBCwithPKCS7.withIV(iv));
 
       // byte[] plain = decryptBytes(inputDecryptor, output);   
         String plaintext =  new String(decryptBytes(inputDecryptor, output));
       
        System.out.println("\u001B[33m"); //yellow
       System.out.println("*************************");
       System.out.println("BC FIPS Decoded Text");
        System.out.println("\u001B[32m"); //green
       System.err.println(plaintext);
       System.out.println("");
       System.out.println("");
       System.out.println("\u001B[0m"); //reset



          }
        catch(Exception e)
	  
         {
		e.printStackTrace();
	 }




    }


       static byte[] encryptBytes(FipsOutputEncryptor outputEncryptor, byte[] plainText) throws IOException
         {
           ByteArrayOutputStream bOut = new ByteArrayOutputStream();
          CipherOutputStream encOut = outputEncryptor.getEncryptingStream(bOut);
           encOut.update(plainText);
          encOut.close();
          return bOut.toByteArray();
 
         }

    static byte[] decryptBytes(FipsInputDecryptor inputDecryptor, byte[] cipherText) throws IOException
       {
         ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         InputStream encIn = inputDecryptor.getDecryptingStream(
          new ByteArrayInputStream(cipherText));
         int ch;
         while ((ch = encIn.read()) >= 0)
           {
              bOut.write(ch);
           }
           return bOut.toByteArray();
      }
 }

