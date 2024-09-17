package com.quside;

import com.quside.QusideEntropySource;

/**
 * Utility class to allow access to information about entropy source
 */
public class EntropyProviderInfo
{
    /**
     * Returns true if Jitter RNG is Available
     *
     * @return
     */
    public static boolean hasJitterRNG()
    {
        try
        {
            return QusideEntropySource.rngAvailable();
        }
        catch (UnsatisfiedLinkError ule)
        {
            return false;
        }
    }

    /**
     * Load status
     *
     * @return
     */
    public static String loaderStatus()
    {
        return "Loaded";
    }

    /**
     * Loaded variant
     *
     * @return
     */
    public static String loadedVariant()
    {
        return "QusideQRNG.so";
    }

    /**
     * Compiler major version loaded.
     *
     * @return
     */
    public static String loadedCompilerMajorVersion()
    {
        return "0";
    }

    /**
     * Info on the compiler than built the currently loaded lib
     *
     * @return
     */
    public static String nativeCompilerInfo()
    {
        try
        {
            return "N/A";
        }
        catch (UnsatisfiedLinkError ule)
        {
            return "Not Loaded";
        }
    }

    /**
     * Native build timestamp
     *
     * @return
     */
    public static String nativeBuildTimeStamp()
    {
        try
        {
            return "N/A";
        }
        catch (UnsatisfiedLinkError ule)
        {
            return "Not Loaded";
        }
    }

    /**
     * Version of Jitter library this native code was built against.
     *
     * @return version string
     */
    public static String jitterLibVersion()
    {
        try
        {
            return "0.1.0";
        }
        catch (UnsatisfiedLinkError ule)
        {
            return "Not Loaded";
        }
    }
}
