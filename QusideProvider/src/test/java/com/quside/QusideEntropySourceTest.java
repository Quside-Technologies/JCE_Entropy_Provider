package com.quside;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;

public class QusideEntropySourceTest {

    @Test
    public void testIsPredictionResistant() {
        QusideEntropySource entropySource = new QusideEntropySource();
        assertTrue(entropySource.isPredictionResistant());
    }

    @Test
    public void testEntropySize() {
        QusideEntropySource entropySource = new QusideEntropySource();
        assertEquals(32, entropySource.entropySize());
    }

    @Test
    public void testGetEntropy() {
        QusideEntropySource entropySource = new QusideEntropySource();
        byte[] entropy = entropySource.getEntropy();

        System.out.println(Arrays.toString(entropy));

        assertNotNull(entropy);
        assertEquals(32, entropy.length);
    }

    @Test
    public void testGetRandomBytes() {
        QusideEntropySource entropySource = new QusideEntropySource();
        byte[] dest = new byte[32];
        long result = entropySource.getRandomBytes(dest);
        
        System.out.println(Arrays.toString(dest));

        assertEquals(32, result);
        assertNotNull(dest);
        assertEquals(32, dest.length);
    }

}

