package com.quside;
import java.security.SecureRandomSpi;

public class QusideQesSpi extends SecureRandomSpi {

    private QusideQRNG qrng = new QusideQRNG();

    @Override
    protected void engineSetSeed(byte[] seed) {
        // Ignore
    }

    @Override
    protected void engineNextBytes(byte[] bytes) {

        // TODO: Check that the QRNG library is loaded.

        int numBoards = qrng.findBoards();
        if (numBoards == 0) {
            throw new UnsupportedOperationException("No QRNG boards found");
        }

        int[] status = new int[1];
        qrng.getCalibrationStatus(0, status);

        int Nuint32 = bytes.length / 4;
        int[] memSlot = new int[Nuint32];

        if (status[0] == 2) {
            qrng.getRandom(memSlot, Nuint32, 0);
        } else {
            throw new UnsupportedOperationException("QRNG is not calibrated");
        }

        for (int i = 0; i < Nuint32; i++) {
            bytes[i * 4] = (byte) (memSlot[i] & 0xFF);
            bytes[i * 4 + 1] = (byte) ((memSlot[i] >> 8) & 0xFF);
            bytes[i * 4 + 2] = (byte) ((memSlot[i] >> 16) & 0xFF);
            bytes[i * 4 + 3] = (byte) ((memSlot[i] >> 24) & 0xFF);
        }

    }

    @Override
    protected byte[] engineGenerateSeed(int numBytes) {
        
        byte[] seed = new byte[numBytes];
        engineNextBytes(seed);
        return seed;
    }
}
