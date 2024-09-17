package com.quside;

/**
 * Base interface describing a provider of entropy sources.
 */
interface EntropySourceProvider
{
    /**
     * Return an entropy source providing block of entropy.
     *
     * @param bitsRequired the size of the block of entropy required.
     * @return an entropy source providing bitsRequired blocks of entropy.
     */
    EntropySource get(final int bitsRequired);
}
