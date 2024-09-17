package com.quside.util;

import com.quside.*;

import java.io.IOException;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

public class DumpInfo
{
    public static void main(String[] args) throws Exception
    {
        if (args.length > 0)
        {
            if (args[0].equals("-c"))
            {
                // -DM System.out.println
                System.out.println(Strings.fromByteArray(Hex.encode(EntropyProviderStatus.getModuleHMAC())));
                // -DM System.err.println
                System.err.println("Generated new HMAC");
            }
            else
            {
                // -DM System.err.println
                System.err.println("Invalid command line arguments.");
            }
        }
        else
        {
            printInfo();
        }
    }

    public static void printInfo() throws Exception
    {
        // -DM System.out.println
        System.out.println(QusideProvider.getInfoString());
        // -DM System.out.println
        System.out.println("Is Ready: " + EntropyProviderStatus.isReady());
        // -DM System.out.println
        System.out.println("Fips Status Message: " + EntropyProviderStatus.getStatusMessage());
        // -DM System.out.println
        System.out.println();
        if (EntropyProviderInfo.hasJitterRNG())
        {
            // -DM System.out.println
            // -DM System.out.println
            // -DM System.out.println
            // -DM System.out.println
            // -DM System.out.println
            // -DM System.out.println
            // -DM System.out.println
            // -DM System.out.println
            // -DM System.out.println
            System.out.println("--------------------------------------------------------------------------------");
            System.out.println("Jitter Supported:");
            System.out.println("    Load Status:                     " + EntropyProviderInfo.loaderStatus());
            System.out.println("    Load Variant:                    " + EntropyProviderInfo.loadedVariant());
            System.out.println("    Loaded Compiler Major Version:   " + EntropyProviderInfo.loadedCompilerMajorVersion());
            System.out.println("    Native Compiler Info:            " + EntropyProviderInfo.nativeCompilerInfo());
            System.out.println("    Native Build Timestamp:          " + EntropyProviderInfo.nativeBuildTimeStamp());
            System.out.println("    Jitter Lib Version:              " + EntropyProviderInfo.jitterLibVersion());
            System.out.println("--------------------------------------------------------------------------------");
        }
        else
        {
            // -DM System.out.println
            System.out.println("Jitter Not Supported");
        }

        // -DM System.out.println
        // -DM System.out.println
        System.out.println("\nModule Checksum:");
        // -DM Hex.toHexString
        System.out.println(Hex.toHexString(EntropyProviderStatus.getModuleHMAC()));

    }





}
