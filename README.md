# Quside QRNG Provider for Java Cryptography Extension (JCE)

The Quside Quantum Random Number Generator (QRNG) provider allows applications to use quantum entropy for cryptographic operations by integrating with Java’s `SecureRandom` class. This document provides instructions on how to install, compile, deploy, and use the Quside QRNG provider.

## Table of Contents
1. [Introduction](#introduction)
2. [Supported Systems](#supported-systems)
3. [Prerequisites](#prerequisites)
4. [Installation](#installation)
5. [Compiling](#compiling)
6. [Deploying](#deploying)
   - [Java 8](#for-java-8)
   - [Java 11 and later](#for-java-11-and-later)
7. [Using the Library](#using-the-library)
8. [Error Handling and Logging](#error-handling-and-logging)
9. [License](#license)
10. [Support](#support)

---

## Introduction

The Quside QRNG provider integrates quantum randomness into Java’s `SecureRandom` API, enabling high-quality entropy sources for cryptographic purposes. The provider can be used alongside other cryptographic providers such as BouncyCastle FIPS, and configured to support Java’s built-in random number generation mechanisms.

---

## Supported Systems

- **Operating Systems**: Linux Ubuntu 20, 22, 24
- **Architectures**: x86_64.
- **Java Versions**: Java 8, 11, and later.

---

## Prerequisites

Before starting, ensure the following dependencies are installed:

1. **Quside QRNG Libraries**: Install the Quside QRNG libraries on your device. Follow the instructions provided with the libraries.
2. **Java Development Kit (JDK)**: Ensure JDK 8, 11, or later is installed, depending on your environment.
3. **Gradle**: Required for building the Quside provider (`gradlew` is included, but `gradle` can be installed system-wide).
4. **Make and GCC**: Required for building the JENT libraries.
5. **BouncyCastle FIPS Provider**: Optional but recommended for FIPS-compliant environments.

---

## Installation

### Installing Quside QRNG Libraries

Ensure the Quside QRNG libraries are correctly installed on your device. These libraries provide the hardware interface necessary for the QRNG entropy source.

Follow the installation instructions provided by Quside to set up the necessary libraries for your system.

---

## Compiling

### Step 1: Build JENT Libraries

Navigate to the `jent` directory and run the following commands:

```bash
cd jent
make
```

This will compile and install the JENT (Jitter Entropy) libraries.

### Step 2: Build Quside QRNG Provider

From the root directory, execute the following command to build the Quside provider:

```bash
./gradlew build
```

This will compile and test the Quside provider, producing the output JAR file.

---

## Deploying

### Step 1: Locate the JAR File

After building, the provider JAR file will be located at:

```
./QusideProvider/build/libs/QusideProvider.jar
```

### Step 2: Update the Provider List

In order to use the Quside QRNG provider, you need to update the `java.security` configuration file. The file is typically found in the `$JAVA_HOME/lib/security/` directory.

#### For Java 8

1. Open the `java.security` file and add the following entry to the security provider list:

    ```properties
    security.provider.1=org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
    security.provider.2=org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
    security.provider.3=com.quside.QusideProvider
    security.provider.4=sun.security.provider.Sun
    ```

2. Update the `securerandom.strongAlgorithms` property to use the Quside QRNG:

    ```properties
    securerandom.strongAlgorithms=QRNG:Quside
    ```

At this point, `SecureRandom.getInstanceStrong()` will return an instance of `SecureRandom` from the Quside provider.

#### For Java 11 and Later

1. Since there is no `jre/lib/ext` directory in Java 11 and later, the BC and Quside provider JARs must be added to the class or module path.
   
2. Update the `java.security` file similarly to Java 8:

    ```properties
    security.provider.1=BCFIPS
    security.provider.2=BCJSSE fips:BCFIPS
    security.provider.3=com.quside.QusideProvider
    ```

3. Set the `securerandom.strongAlgorithms` property:

    ```properties
    securerandom.strongAlgorithms=QRNG:Quside
    ```

---

## Using the Library

After deployment, the Quside QRNG provider can be used in Java applications. Below is an example of how to retrieve a cryptographic-strength random number generator using the Quside provider.

```java
import java.security.SecureRandom;

public class QRNGExample {
    public static void main(String[] args) throws Exception {
        // Get an instance of SecureRandom using the Quside QRNG provider
        SecureRandom secureRandom = SecureRandom.getInstanceStrong();
        
        // Generate random bytes
        byte[] randomBytes = new byte[16];
        secureRandom.nextBytes(randomBytes);
        
        System.out.println("Generated random bytes: " + bytesToHex(randomBytes));
    }
    
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
```

In this example, `SecureRandom.getInstanceStrong()` will use the Quside QRNG as the entropy source.

---

## Error Handling and Logging

If the Quside provider fails to initialize, Java’s `SecureRandom` will throw a `NoSuchAlgorithmException`. Ensure that:

- The Quside QRNG libraries are properly installed.
- The `java.security` file is correctly configured.
- The JAR files are in the appropriate class/module paths.

---

## License

© 2024 Quside Technologies SL. All rights reserved.

This software and associated documentation files (the "Software") are proprietary to Quside Technologies SL. Unauthorized copying, modification, distribution, or use of the Software, in whole or in part, is strictly prohibited.

For licensing inquiries, please contact [hello@quside.com](mailto:hello@quside.com).

---

## Support

For further assistance, please reach out to [support@quside.com](mailto:support@quside.com). 

For bug reports or feature requests, you can also file an issue on our [GitHub repository](https://github.com/quside/QRNG).



_(C) 2024 Quside Technologies SL. All rights reserved._
