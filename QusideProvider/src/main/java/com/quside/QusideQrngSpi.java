package com.quside;

import java.security.SecureRandomSpi;


public class QusideQrngSpi extends SecureRandomSpi {

    private final QusideEntropySource entropySource;
    private final HashSP800DRBG drbg;

    QusideQrngSpi(QusideEntropySource qes)
    {
        this.entropySource = qes;

        final byte[] perso = new byte[0];

        int size = qes.entropySize();

        byte[] nonce = new byte[size];

        this.entropySource.getRandomBytes(nonce);
        
        drbg = new HashSP800DRBG(
               new SHA512Digest(),
               entropySource.entropySize(),
               entropySource, perso, nonce);
    }


    @Override
    protected void engineSetSeed(byte[] seed)
    {
        // Does nothing
    }

    @Override
    protected void engineNextBytes(byte[] bytes)
    {
        drbg.generate(bytes, null, true);
    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes)
    {
        byte[] seed = new byte[numBytes];
        entropySource.getRandomBytes(seed);
        return seed;
    }


    @Override
    public String toString()
    {
        return "QusideQrngSpi["+entropySource.toString()+"]";
    }
}
