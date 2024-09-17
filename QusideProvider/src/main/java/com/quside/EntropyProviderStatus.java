package com.quside;

import com.quside.util.Arrays;
import com.quside.util.Hex;
import com.quside.util.Pack;
import com.quside.util.Strings;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class EntropyProviderStatus
{

    public static final String MODULE_HMAC_KEY = "Legion of the Bouncy Castle Inc.";
    private static Throwable statusException;
    private static final AtomicBoolean readyStatus = new AtomicBoolean(false);
    private static final Object statusLock = new Object();
    private static final String READY = "READY";

    public static boolean isReady()
    {
        synchronized (statusLock)
        {
            try
            {
                HashSP800DRBG.selfTest();
            }
            catch (Exception ex)
            {
                statusException = ex;
                return false;
            }


            readyStatus.set(true);

            return readyStatus.get();
        }
    }

    public static Throwable getErrorThrowable()
    {
        return statusException;
    }

    public static void fail(String message)
    {
        synchronized (statusLock)
        {
            statusException = new EntropyProviderOperationException(message);
            throw (RuntimeException) statusException;
        }
    }

    public static String getStatusMessage()
    {
        try
        {
            isReady();
        }
        catch (EntropyProviderOperationException foe)
        {
            // ignore as loader exception will now be set.
        }

        if (statusException != null)
        {
            return statusException.getMessage();
        }

        return READY;

    }

    private static void checksumValidate()
    {
        final String rscName = AccessController.doPrivileged(new PrivilegedAction<String>()
        {
            public String run()
            {
                return getResourceName();
            }
        });

        if (rscName == null)
        {
            moveToErrorStatus("Module checksum failed: unable to find");
        }

        if (rscName.startsWith("jrt:/"))
        {
            moveToErrorStatus("Module checksum failed: unable to calculate");
        }
        else
        {
            JarFile jarFile = AccessController.doPrivileged(new PrivilegedAction<JarFile>()
            {
                public JarFile run()
                {
                    try
                    {
                        return new JarFile(rscName);
                    }
                    catch (IOException e)
                    {
                        return null;
                    }
                }
            });

            if (jarFile != null)      // we only do the checksum calculation if we are running off a jar file.
            {
                try
                {
                    byte[] hmac = calculateModuleHMAC(jarFile);
                    InputStream macIn = jarFile.getInputStream(jarFile.getEntry("META-INF/HMAC.SHA512"));

                    StringBuilder sb = new StringBuilder(hmac.length * 2);

                    int ch;
                    while ((ch = macIn.read()) >= 0 && ch != '\r' && ch != '\n')
                    {
                        sb.append((char) ch);
                    }

                    byte[] fileMac = Hex.decode(sb.toString().trim());

                    if (!Arrays.constantTimeAreEqual(hmac, fileMac))
                    {
                        moveToErrorStatus("Module checksum failed: expected [" + sb.toString().trim() + "] got [" + Strings.fromByteArray(Hex.encode(hmac)) + "]");
                    }
                }
                catch (Exception e)
                {
                    statusException = e;

                    moveToErrorStatus(new EntropyProviderOperationException("Module checksum failed: " + e.getMessage(), e));
                }
            }
        }
    }


    private static byte[] calculateModuleHMAC(JarFile jarFile)
    {
        // this code is largely the standard approach to self verifying a JCE with some minor modifications. It will calculate
        // the SHA-256 HMAC on the classes.
        try
        {
            HMac hMac = new HMac(new SHA512Digest());

            hMac.init(new KeyParameter(Strings.toByteArray(MODULE_HMAC_KEY)));

            // build an index to make sure we get things in the same order.
            Map<String, JarEntry> index = new TreeMap<String, JarEntry>();

            for (Enumeration<JarEntry> entries = jarFile.entries(); entries.hasMoreElements(); )
            {
                JarEntry jarEntry = entries.nextElement();

                // Skip directories, META-INF, and module-info.class meta-data
                if (jarEntry.isDirectory()
                        || (jarEntry.getName().startsWith("META-INF/") && jarEntry.getName().indexOf("versions") < 0)
                        || jarEntry.getName().indexOf("module-info.class") > 0)
                {
                    continue;
                }

                Object last = index.put(jarEntry.getName(), jarEntry);
                if (last != null)
                {
                    IllegalStateException e = new IllegalStateException("Unable to initialize module: duplicate entry found in jar file");
                    statusException = e;
                    throw e;
                }
            }

            byte[] buf = new byte[8192];
            for (Map.Entry<String, JarEntry> entry : index.entrySet())
            {
                JarEntry jarEntry = entry.getValue();
                InputStream is = jarFile.getInputStream(jarEntry);

                // Read in each jar entry. A SecurityException will
                // be thrown if a signature/digest check fails - if that happens
                // we'll just return an empty checksum

                // header information
                byte[] encName = Strings.toUTF8ByteArray(jarEntry.getName());
                hMac.update((byte) 0x5B);   // '['
                hMac.update(encName, 0, encName.length);
                hMac.update(Pack.longToBigEndian(jarEntry.getSize()), 0, 8);
                hMac.update((byte) 0x5D);    // ']'

                // contents
                int n;
                while ((n = is.read(buf, 0, buf.length)) != -1)
                {
                    hMac.update(buf, 0, n);
                }
                is.close();
            }

            hMac.update((byte) 0x5B);   // '['
            byte[] encName = Strings.toUTF8ByteArray("END");
            hMac.update(encName, 0, encName.length);
            hMac.update((byte) 0x5D);    // ']'

            byte[] hmacResult = new byte[hMac.getMacSize()];

            hMac.doFinal(hmacResult, 0);

            return hmacResult;
        }
        catch (Exception e)
        {
            return new byte[32];
        }
    }

    private static String getResourceName()
    {
        // we use the MARKER file, at the same level in the class hierarchy as this
        // class, to find the enclosing Jar file (if one exists)

        String result = null;

        final String markerName = QusideProvider.class.getCanonicalName().replace(".", "/").replace("QusideProvider", "MARKER");
        final String marker = getMarker(QusideProvider.class, markerName);


        if (marker != null)
        {
            if (marker.startsWith("jar:") && marker.contains("!/"))
            {
                try
                {
                    int secondColon = marker.indexOf(':', 4);
                    if (secondColon == -1)
                    {
                        return null;
                    }
                    String jarFilename = URLDecoder.decode(marker.substring(secondColon + 1, marker.lastIndexOf("!/")), "UTF-8");

                    result = jarFilename;
                }
                catch (IOException e)
                {
                    // we found our jar file, but couldn't open it
                    result = null;
                }
            }
            else if (marker.startsWith("file:") && marker.endsWith(".jar"))
            {
                try
                {
                    String jarFilename = URLDecoder.decode(marker.substring("file:".length()), "UTF-8");

                    result = jarFilename;
                }
                catch (IOException e)
                {
                    // we found our jar file, but couldn't open it
                    result = null;
                }
            }
            else if (marker.startsWith("jrt:"))
            {
                return marker;
            }
            else if (marker.startsWith("file:"))
            {
                return marker;    // this means we're running from classes (development)
            }
        }

        return result;
    }

    static String getMarker(final Class sourceClass, final String markerName)
    {
        ClassLoader loader = sourceClass.getClassLoader();

        if (loader != null)
        {
            Object resource = AccessController.doPrivileged(
                    new PrivilegedAction()
                    {
                        public Object run()
                        {
                            try
                            {
                                CodeSource cs =
                                        sourceClass.getProtectionDomain().getCodeSource();
                                return cs.getLocation();
                            }
                            catch (Exception e)
                            {
                                return null;
                            }
                        }
                    });
            if (resource != null)
            {
                return resource.toString();
            }

            return loader.getResource(markerName).toString();
        }
        else
        {
            return AccessController.doPrivileged(new PrivilegedAction<String>()
            {
                public String run()
                {
                    return ClassLoader.getSystemResource(markerName).toString();
                }
            });
        }
    }

    static void moveToErrorStatus(String error)
    {
        moveToErrorStatus(new EntropyProviderOperationException(error));
    }

    static void moveToErrorStatus(EntropyProviderOperationException error)
    {
        // FSM_STATE:8.0
        // FSM_TRANS:3.2
        statusException = error;
        throw (EntropyProviderOperationException) statusException;
    }

    public static byte[] getModuleHMAC()
    {
        try
        {
            String rscName = getResourceName();
            return calculateModuleHMAC(new JarFile(rscName));
        }
        catch (Exception e)
        {
            return new byte[64];
        }
    }
}
