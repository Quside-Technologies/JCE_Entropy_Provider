package com.quside;

/**
 * JitterEntropySource
 * !! IF YOU RENAME THIS CLASS you will need to adjust jent_jni.c
 */
class QusideEntropySource implements EntropySource
{
    private static QusideQRNG qes;
    private static final Object jentLock = new Object();

    private final int byteSize;

    static {
        qes = new QusideQRNG();
    }

    QusideEntropySource() {
        this.byteSize = 32;
    }

    @Override
    public boolean isPredictionResistant()
    {
        return true;
    }

    @Override
    public byte[] getEntropy()
    {
        byte[] entropy = new byte[byteSize];
        long retLen = getRandomBytes(entropy, 0, entropy.length);
        assert retLen == byteSize;
        return entropy;
    }

    @Override
    public int entropySize()
    {
        return byteSize;
    }

    /**
     * Get random bytes,retrying until buffer is full.
     *
     * @param dest  the target array
     * @param start the start index
     * @param len   number of bytes
     * @return the amount read.
     */
    long getRandomBytes(byte[] dest, int start, int len)
    {
        // TODO: Check that the QRNG library is loaded.

        int numBoards = qes.findBoards();
        if (numBoards == 0) {
            throw new UnsupportedOperationException("No QRNG boards found");
        }

        int[] status = new int[1];
        qes.getCalibrationStatus(0, status);

        int Nuint32 = dest.length / 4;
        int[] memSlot = new int[Nuint32];

        if (status[0] == 2) {
            qes.getRandom(memSlot, Nuint32, 0);
        } else {
            throw new UnsupportedOperationException("QRNG is not calibrated");
        }

        for (int i = 0; i < Nuint32; i++) {
            dest[i * 4] = (byte) (memSlot[i] & 0xFF);
            dest[i * 4 + 1] = (byte) ((memSlot[i] >> 8) & 0xFF);
            dest[i * 4 + 2] = (byte) ((memSlot[i] >> 16) & 0xFF);
            dest[i * 4 + 3] = (byte) ((memSlot[i] >> 24) & 0xFF);
        }

        return len;
    }


    /**
     * Get random bytes,retrying until buffer is full.
     *
     * @param dest the target array
     * @return the amount read.
     */
    long getRandomBytes(byte[] dest)
    {
        return getRandomBytes(dest, 0, dest.length);
    }

    /**
     * Returns true of the native rng is available.
     *
     * @return true if available
     */
    protected static boolean rngAvailable()
    {
        synchronized (jentLock)
        {
            try
            {
                // TODO: return availability of Quside RNG
                return true;
            }
            catch (UnsatisfiedLinkError ule)
            {
                return false;
            }
        }
    }




}
