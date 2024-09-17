package com.quside;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class QusideQRNGTest {

    @Test
    public void testFindBoards() {
        QusideQRNG qrng = new QusideQRNG();
        int numOfBoards = qrng.findBoards();
        assertTrue(numOfBoards >= 0);
    }

    @Test
    public void testGetRandom() {
        QusideQRNG qrng = new QusideQRNG();
        int Nuint32 = 1024;
        int[] memSlot = new int[Nuint32];
        int devInd = 0;
        int result = qrng.getRandom(memSlot, Nuint32, devInd);
        assertEquals(0, result);
        assertNotNull(memSlot);
        assertEquals(Nuint32, memSlot.length);
    }

    @Test
    public void testQualityQFactor() {
        QusideQRNG qrng = new QusideQRNG();
        float[] qFactor = new float[1];
        int devInd = 0;
        int result = qrng.qualityQFactor(devInd, qFactor);
        assertEquals(0, result);
        assertNotNull(qFactor);
        assertEquals(1, qFactor.length);
    }

    @Test
    public void testGetHmin() {
        QusideQRNG qrng = new QusideQRNG();
        float[] hMin = new float[1];
        int devInd = 0;
        int result = qrng.getHmin(devInd, hMin);
        assertEquals(0, result);
        assertNotNull(hMin);
        assertEquals(1, hMin.length);
    }

    @Test
    public void testGetCalibrationStatus() {
        QusideQRNG qrng = new QusideQRNG();
        int[] status = new int[1];
        int devInd = 0;
        int result = qrng.getCalibrationStatus(devInd, status);
        assertEquals(0, result);
        assertNotNull(status);
        assertEquals(1, status.length);
    }

    @Test
    public void testWholeQRNGWorkflow() {

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

        int Nuint32 = 1024;
        int[] memSlot = new int[Nuint32];
        if (status[0] == 2) {
            qrng.getRandom(memSlot, Nuint32, 0);
            System.out.println("Random number [0]: " + memSlot[0]);
        }

    }

}