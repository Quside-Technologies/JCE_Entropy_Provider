package com.quside;

public class QusideQRNG {
    static {
        try {
            System.loadLibrary("QusideQRNG");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Failed to load native library: " + e.getMessage());
            throw e;
        }
    }

    // Native method declarations
    public native int findBoards();
    public native int getRandom(int[] memSlot, int Nuint32, int devInd);
    public native int qualityQFactor(int devInd, float[] qFactor);
    public native int getHmin(int devInd, float[] hMin);
    public native int getCalibrationStatus(int devInd, int[] status);
    public native int setCalibration(int devInd);

    // Load the native library
    public static void main(String[] args) {
        // Example usage
        QusideQRNG qrng = new QusideQRNG();
        System.out.println("Number of boards: " + qrng.findBoards());

        float[] qFactor = new float[1];
        qrng.qualityQFactor(0, qFactor);

        System.out.println("Quality factor: " + qFactor[0]);

        float[] hMin = new float[1];
        qrng.getHmin(0, hMin);

        System.out.println("Hmin: " + hMin[0]);

        int[] status = new int[1];
        qrng.getCalibrationStatus(0, status);

        System.out.println("Calibration status: " + status[0]);

        int Nuint32 = 16;
        int[] memSlot = new int[Nuint32];
        if (status[0] == 2) {
            qrng.getRandom(memSlot, Nuint32, 0);
            System.out.println("Random number: " + memSlot[0]);
        }

    }

}
