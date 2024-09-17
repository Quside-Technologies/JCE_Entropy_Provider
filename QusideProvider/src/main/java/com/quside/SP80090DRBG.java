package com.quside;

interface SP80090DRBG extends DRBG
{
    /**
     * Return the block size of the DRBG.
     *
     * @return the block size (in bits) produced by each round of the DRBG.
     */
    int getBlockSize();

    /**
     * Return the personalization string used to create the DRBG.
     *
     * @return the personalization string used to create the DRBG.
     */
    byte[] getPersonalizationString();

    /**
     * Populate a passed in array with random data.
     *
     * @param output output array for generated bits.
     * @param additionalInput additional input to be added to the DRBG in this step.
     * @param predictionResistant true if a reseed should be forced, false otherwise.
     *
     * @return number of bits generated, -1 if a reseed required.
     */
    int generate(byte[] output, byte[] additionalInput, boolean predictionResistant);

    /**
     * Reseed the DRBG.
     *
     * @param additionalInput additional input to be added to the DRBG in this step.
     */
    void reseed(byte[] additionalInput);

}