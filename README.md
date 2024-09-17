# Compiling

Quside QRNG libraries should be installed in the device. Please follow the instructions provided with the libraries to install them.

Once installed, to install the Java JENT libraries, do the following:

cd jent
make

this should build and install the JENT libraries.

For the Quside Provider, do the following in the root directory:

./gradlew build

this should build and test the provider.

# Deploying

The JENT library and provider appears in its own jar, currently:

./QusideProvider/build/libs/QusideProvider.jar

The provider implementation appears in:

com.quside.QusideProvider

with the name "QRNG".

# Deploying the RNG provider.

## For Java 8

Usually the BCFIPS install results in the provider list in the java.security file starting with:

security.provider.1=org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
security.provider.2=org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
security.provider.3=sun.security.provider.Sun

With securerandom.strongAlgorithms set to:

securerandom.strongAlgorithms=NativePRNGBlocking:SUN

Usually NativePRNGBlocking is either pointing at /dev/random or /dev/hwrng as its seed generator and the BCFIPS provider uses the generateSeed() method on the SecureRandom returned by the SecureRandom.getInstanceStrong() method to access the seed generator of the algorithm provider by securerandom.strongAlgorithms.

This implies a couple of things: 
 - the SUN provider needs to be present for securerandom.strongAlgorithms to be meaningful when it has its default value.
 - changing the default value of secureRandom.strongAlgorithms requires including the necessary provider in the security.provider list.

As this is the case introducing the Quside provider requires adding:

com.quside.QusideProvider

to the security provider list, and then setting securerandom.strongAlgorithms accordingly.

Usually you will want to maintain the DEFAULT RNG as the one produced by the FIPS provider, in that case you would add the QRNG provider later in the list, as in:

security.provider.1=org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider
security.provider.2=org.bouncycastle.jsse.provider.BouncyCastleJsseProvider
security.provider.3=com.quside.QusideProvider
security.provider.4=sun.security.provider.Sun

and then securerandom.strongAlgorithms would be set to:

securerandom.strongAlgorithms=QRNG:Quside

At this point SecureRandom.getInstanceStrong() will then return an instance of the SecureRandom created by the Quside provider, with the generateSeed() method providing direct access to the actual Quside's QRNG entropy output.

## For Java 11 and later

In the case of Java 11 and later, the BCFIPS install results in the provider list in the java.security file starting with:

security.provider.1=BCFIPS
security.provider.2=BCJSSE fips:BCFIPS
security.provider.3=com.quside.QusideProvider

Unlike with Java 8 the BC jars need to be on the class path or module path as there is no jre/lib/ext directory to add them. After that things are basically the same. The JENT jar needs to be added to the class/module path and the securerandom.strongAlgorithms property in java.security needs to be set to reference the BCRNG in the same way as before, i.e:

securerandom.strongAlgorithms=QRNG:Quside
