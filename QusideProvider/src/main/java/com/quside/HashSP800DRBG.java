package com.quside;

import com.quside.util.Arrays;
import com.quside.util.Hex;

import java.util.HashMap;
import java.util.Map;

class HashSP800DRBG
        implements SP80090DRBG
{
    private final static byte[] ZERO = {0x00};
    private final static byte[] ONE = {0x01};

    private final static long RESEED_MAX = 1L << (48 - 1);
    private final static int MAX_BITS_REQUEST = 1 << (19 - 1);

    private final static Map<String,Integer> seedlens = new  HashMap<String,Integer>();
    private final static Map<String, byte[][]> kats = new HashMap<String, byte[][]>();
    private final static Map<String, byte[]> reseedVs = new HashMap<String, byte[]>();
    private final static Map<String, byte[][]> reseedKats = new HashMap<String, byte[][]>();


    static
    {
        seedlens.put("SHA-512", 888);
        kats.put("SHA-512", new byte[][]{
                Hex.decode("ca8387ba70bc7f8cb71e5d25703972ed58c7b5c81649050cdc17a9f646f7bd57857ca715e411d2ca"),
                Hex.decode("ce2fe5ba54cde888bee0f4863ca70b258ab6e2be31523542a4da66033433fb8e7e394b28198daa1e")});

        reseedVs.put("SHA-512", Hex.decode("397118fdac8d83ad98813c50759c85b8c47565d8268bf10da483153b747a74743a58a90e85aa9f705ce6984ffc128db567489817e4092d050d8a1cc596ddc119"));

        reseedKats.put("SHA-512", new byte[][]{
                Hex.decode("147abe77d9b19bf6331691eeb3571e55afb406d1ddcd7aa5f1b3de71f0d3eb6949ea580764588000"),
                Hex.decode("59c18dd408b82f930411bfdeea503d0154a77263c934d7888677ce34018307d4dd035effed210979")});
    }

    private Digest _digest;
    private WorkingBuffer workingBuf = new WorkingBuffer();
    private long _reseedCounter;
    private EntropySource _entropySource;
    private int _securityStrength;
    private int _seedLength;
    private byte[] _personalizationString;

    /**
     * Construct a SP800-90A Hash DRBG.
     * <p>
     * Minimum entropy requirement is the security strength requested.
     * </p>
     *
     * @param digest                source digest to use for DRB stream.
     * @param securityStrength      security strength required (in bits)
     * @param entropySource         source of entropy to use for seeding/reseeding.
     * @param personalizationString personalization string to distinguish this DRBG (may be null).
     * @param nonce                 nonce to further distinguish this DRBG (may be null).
     */
    public HashSP800DRBG(SHA512Digest digest, int securityStrength, EntropySource entropySource, byte[] personalizationString, byte[] nonce)
    {
        init(digest, securityStrength, entropySource, personalizationString, nonce);
    }

    /**
     * Used to self test on module startup.
     */
    static void selfTest()
    {
        HashSP800DRBG drbg = new HashSP800DRBG(new SHA512Digest(), 256, new DRBGUtils.KATEntropyProvider().get(256), new byte[256], new byte[256]);
        drbg.doSelfTest();
        drbg.doReseedSelfTest();
    }


    private void init(Digest digest, int securityStrength, EntropySource entropySource, byte[] personalizationString, byte[] nonce)
    {
        if (securityStrength > DRBGUtils.getMaxSecurityStrength(digest))
        {
            throw new IllegalArgumentException("Requested security strength is not supported by the derivation function");
        }

        if (entropySource.entropySize() < securityStrength)
        {
            throw new IllegalArgumentException("Not enough entropy for security strength required");
        }

        _digest = digest;
        _entropySource = entropySource;
        _securityStrength = securityStrength;
        _personalizationString = Arrays.clone(personalizationString);
        _seedLength = ((Integer) seedlens.get(digest.getAlgorithmName())).intValue();


        // 1. seed_material = entropy_input || nonce || personalization_string.
        // 2. seed = Hash_df (seed_material, seedlen).
        // 3. V = seed.
        // 4. C = Hash_df ((0x00 || V), seedlen). Comment: Preceed V with a byte
        // of zeros.
        // 5. reseed_counter = 1.
        // 6. Return V, C, and reseed_counter as the initial_working_state

        byte[] entropy = getEntropy();
        byte[] seedMaterial = Arrays.concatenate(entropy, nonce, personalizationString);
        Arrays.fill(entropy, (byte) 0);

        reseedFromSeedMaterial(seedMaterial);
    }

    /**
     * Return the block size (in bits) of the DRBG.
     *
     * @return the number of bits produced on each internal round of the DRBG.
     */
    public int getBlockSize()
    {
        return _digest.getDigestSize() * 8;
    }

    /**
     * Return the security strength of the DRBG.
     *
     * @return the security strength (in bits) of the DRBG.
     */
    public int getSecurityStrength()
    {
        return _securityStrength;
    }

    /**
     * Return the personalization string used to create the DRBG.
     *
     * @return the personalization string used to create the DRBG.
     */
    public byte[] getPersonalizationString()
    {
        return Arrays.clone(_personalizationString);
    }

    /**
     * Populate a passed in array with random data.
     *
     * @param output              output array for generated bits.
     * @param additionalInput     additional input to be added to the DRBG in this step.
     * @param predictionResistant true if a reseed should be forced, false otherwise.
     * @return number of bits generated, -1 if a reseed required.
     */
    public int generate(byte[] output, byte[] additionalInput, boolean predictionResistant)
    {
        // 1. If reseed_counter > reseed_interval, then return an indication that a
        // reseed is required.
        // 2. If (additional_input != Null), then do
        // 2.1 w = Hash (0x02 || V || additional_input).
        // 2.2 V = (V + w) mod 2^seedlen
        // .
        // 3. (returned_bits) = Hashgen (requested_number_of_bits, V).
        // 4. H = Hash (0x03 || V).
        // 5. V = (V + H + C + reseed_counter) mod 2^seedlen
        // .
        // 6. reseed_counter = reseed_counter + 1.
        // 7. Return SUCCESS, returned_bits, and the new values of V, C, and
        // reseed_counter for the new_working_state.
        int numberOfBits = output.length * 8;

        if (numberOfBits > MAX_BITS_REQUEST)
        {
            throw new IllegalArgumentException("Number of bits per request limited to " + MAX_BITS_REQUEST);
        }

        if (predictionResistant)
        {
            reseed(additionalInput);
            additionalInput = null;
        }

        if (_reseedCounter > RESEED_MAX)
        {
            return -1;
        }

        // 2.
        if (additionalInput != null)
        {
            byte[] newInput = new byte[1 + workingBuf._V.length + additionalInput.length];
            newInput[0] = 0x02;
            System.arraycopy(workingBuf._V, 0, newInput, 1, workingBuf._V.length);
            // TODO: inOff / inLength
            System.arraycopy(additionalInput, 0, newInput, 1 + workingBuf._V.length, additionalInput.length);
            byte[] w = hash(newInput);

            addTo(workingBuf._V, w);
        }

        // 3.
        byte[] rv = hashgen(workingBuf._V, numberOfBits);

        // 4.
        byte[] subH = new byte[workingBuf._V.length + 1];
        System.arraycopy(workingBuf._V, 0, subH, 1, workingBuf._V.length);
        subH[0] = 0x03;

        byte[] H = hash(subH);

        // 5.
        addTo(workingBuf._V, H);
        addTo(workingBuf._V, workingBuf._C);
        byte[] c = new byte[4];
        c[0] = (byte) (_reseedCounter >> 24);
        c[1] = (byte) (_reseedCounter >> 16);
        c[2] = (byte) (_reseedCounter >> 8);
        c[3] = (byte) _reseedCounter;

        addTo(workingBuf._V, c);

        _reseedCounter++;

        System.arraycopy(rv, 0, output, 0, output.length);

        return numberOfBits;
    }

    // this will always add the shorter length byte array mathematically to the
    // longer length byte array.
    // be careful....
    private void addTo(byte[] longer, byte[] shorter)
    {
        int carry = 0;
        for (int i = 1; i <= shorter.length; i++) // warning
        {
            int res = (longer[longer.length - i] & 0xff) + (shorter[shorter.length - i] & 0xff) + carry;
            carry = (res > 0xff) ? 1 : 0;
            longer[longer.length - i] = (byte) res;
        }

        for (int i = shorter.length + 1; i <= longer.length; i++) // warning
        {
            int res = (longer[longer.length - i] & 0xff) + carry;
            carry = (res > 0xff) ? 1 : 0;
            longer[longer.length - i] = (byte) res;
        }
    }

    /**
     * Reseed the DRBG.
     *
     * @param additionalInput additional input to be added to the DRBG in this step.
     */
    public void reseed(byte[] additionalInput)
    {
        // 1. seed_material = 0x01 || V || entropy_input || additional_input.
        //
        // 2. seed = Hash_df (seed_material, seedlen).
        //
        // 3. V = seed.
        //
        // 4. C = Hash_df ((0x00 || V), seedlen).
        //
        // 5. reseed_counter = 1.
        //
        // 6. Return V, C, and reseed_counter for the new_working_state.
        //
        // Comment: Precede with a byte of all zeros.

        byte[] entropy = getEntropy();
        byte[] seedMaterial = Arrays.concatenate(ONE, workingBuf._V, entropy, additionalInput);
        Arrays.fill(entropy, (byte) 0);

        reseedFromSeedMaterial(seedMaterial);
    }

    private void reseedFromSeedMaterial(byte[] seedMaterial)
    {
        workingBuf._V = hashSeedMaterial(seedMaterial);
        workingBuf._C = hashSeedMaterial(Arrays.concatenate(ZERO, workingBuf._V));
        _reseedCounter = 1;
    }

    private byte[] hashSeedMaterial(byte[] seedMaterial)
    {
        try
        {
            return DRBGUtils.hash_df(_digest, seedMaterial, _seedLength);
        }
        finally
        {
            Arrays.fill(seedMaterial, (byte) 0);
        }
    }

    private byte[] getEntropy()
    {
        byte[] entropy = _entropySource.getEntropy();
        if (entropy == null || entropy.length < (_securityStrength + 7) / 8)
        {
            throw new IllegalStateException("Insufficient entropy provided by entropy source");
        }
        return entropy;
    }


    public void doSelfTest() throws EntropyProviderOperationException
    {
        byte[] origV = workingBuf._V;
        byte[] origC = workingBuf._C;
        byte[] personalizationString = _personalizationString;
        long origReseedCounter = _reseedCounter;
        EntropySource origEntropySource = _entropySource;
        int origSeedLength = _seedLength;
        int origSecurityStrength = _securityStrength;

        try
        {
            byte[] personalization = Hex.decode("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F606162636465666768696A6B6C6D6E6F70717273747576");
            byte[] nonce = Hex.decode("2021222324");

            final int entropyStrength = DRBGUtils.getMaxSecurityStrength(_digest);

            byte[][] expected = kats.get(_digest.getAlgorithmName());

            init(_digest, _securityStrength, new DRBGUtils.KATEntropyProvider().get(entropyStrength), personalization, nonce);

            byte[] output = new byte[expected[0].length];

            generate(output, null, true);
            if (!Arrays.areEqual(expected[0], output))
            {
                EntropyProviderStatus.fail("DRBG Block 1 KAT failure");
            }

            output = new byte[expected[1].length];

            generate(output, null, true);
            if (!Arrays.areEqual(expected[1], output))
            {
                EntropyProviderStatus.fail("DRBG Block 2 KAT failure");
            }

            try
            {
                init(_digest, _securityStrength, new DRBGUtils.LyingEntropySource(entropyStrength), personalization, nonce);

                EntropyProviderStatus.fail("DRBG LyingEntropySource not detected in init");
            }
            catch (IllegalStateException e)
            {
                if (!e.getMessage().equals("Insufficient entropy provided by entropy source"))
                {
                    EntropyProviderStatus.fail("DRBG self test failed init entropy check");
                }
            }

            try
            {
                init(_digest, _securityStrength, new DRBGUtils.LyingEntropySource(20), personalization, nonce);

                EntropyProviderStatus.fail("DRBG insufficient EntropySource not detected");
            }
            catch (IllegalArgumentException e)
            {
                if (!e.getMessage().equals("Not enough entropy for security strength required"))
                {
                    EntropyProviderStatus.fail("DRBG self test failed init entropy check");
                }
            }

            try
            {
                _entropySource = new DRBGUtils.LyingEntropySource(entropyStrength);

                reseed(null);

                EntropyProviderStatus.fail("DRBG LyingEntropySource not detected in reseed");
            }
            catch (IllegalStateException e)
            {
                if (!e.getMessage().equals("Insufficient entropy provided by entropy source"))
                {
                    EntropyProviderStatus.fail("DRBG self test failed reseed entropy check");
                }
            }

            try
            {
                init(_digest, entropyStrength + 1, new DRBGUtils.KATEntropyProvider().get(entropyStrength), personalization, nonce);

                EntropyProviderStatus.fail("DRBG successful initialise with too high security strength");
            }
            catch (IllegalArgumentException e)
            {
                if (!e.getMessage().equals("Requested security strength is not supported by the derivation function"))
                {
                    EntropyProviderStatus.fail("DRBG self test failed init security strength check");
                }
            }
        }
        finally
        {
            workingBuf._V = origV;
            workingBuf._C = origC;
            _personalizationString = personalizationString;
            _reseedCounter = origReseedCounter;
            _entropySource = origEntropySource;
            _seedLength = origSeedLength;
            _securityStrength = origSecurityStrength;
        }

    }

    public void doReseedSelfTest() throws EntropyProviderOperationException
    {
        byte[] origV = workingBuf._V;
        byte[] origC = workingBuf._C;
        byte[] personalizationString = _personalizationString;
        long origReseedCounter = _reseedCounter;
        EntropySource origEntropySource = _entropySource;
        int origSeedLength = _seedLength;
        int origSecurityStrength = _securityStrength;

        try
        {
            byte[] additionalInput = Hex.decode("404142434445464748494A4B4C4D4E4F505152535455565758595A5B5C5D5E5F606162636465666768696A6B6C6D6E6F70717273747576");

            int entropyStrength = DRBGUtils.getMaxSecurityStrength(_digest);

            byte[][] expected = reseedKats.get(_digest.getAlgorithmName());

            workingBuf._V = Arrays.clone(reseedVs.get(_digest.getAlgorithmName()));

            _entropySource = new DRBGUtils.KATEntropyProvider().get(entropyStrength);

            reseed(additionalInput);

            if (_reseedCounter != 1)
            {
                EntropyProviderStatus.fail("DRBG reseedCounter failed to reset");
            }

            byte[] output = new byte[expected[0].length];

            generate(output, null, false);
            if (!Arrays.areEqual(expected[0], output))
            {
                EntropyProviderStatus.fail("DRBG Block 1 reseed KAT failure");
            }

            output = new byte[expected[1].length];

            generate(output, null, false);
            if (!Arrays.areEqual(expected[1], output))
            {
                EntropyProviderStatus.fail("DRBG Block 2 reseed KAT failure");
            }

            try
            {
                _entropySource = new DRBGUtils.LyingEntropySource(entropyStrength);

                reseed(null);

                EntropyProviderStatus.fail("DRBG LyingEntropySource not detected on reseed");
            }
            catch (IllegalStateException e)
            {
                if (!e.getMessage().equals("Insufficient entropy provided by entropy source"))
                {
                    EntropyProviderStatus.fail("DRBG self test failed reseed entropy check");
                }
            }
        }
        finally
        {
            workingBuf._V = origV;
            workingBuf._C = origC;
            _personalizationString = personalizationString;
            _reseedCounter = origReseedCounter;
            _entropySource = origEntropySource;
            _seedLength = origSeedLength;
            _securityStrength = origSecurityStrength;
        }

    }

    private byte[] hash(byte[] input)
    {
        byte[] hash = new byte[_digest.getDigestSize()];
        doHash(input, hash);
        return hash;
    }

    private void doHash(byte[] input, byte[] output)
    {
        _digest.update(input, 0, input.length);
        _digest.doFinal(output, 0);
    }

    // 1. m = [requested_number_of_bits / outlen]
    // 2. data = V.
    // 3. W = the Null string.
    // 4. For i = 1 to m
    // 4.1 wi = Hash (data).
    // 4.2 W = W || wi.
    // 4.3 data = (data + 1) mod 2^seedlen
    // .
    // 5. returned_bits = Leftmost (requested_no_of_bits) bits of W.
    private byte[] hashgen(byte[] input, int lengthInBits)
    {
        int digestSize = _digest.getDigestSize();
        int m = (lengthInBits / 8) / digestSize;

        byte[] data = new byte[input.length];
        System.arraycopy(input, 0, data, 0, input.length);

        byte[] W = new byte[lengthInBits / 8];

        byte[] dig = new byte[_digest.getDigestSize()];
        for (int i = 0; i <= m; i++)
        {
            doHash(data, dig);

            int bytesToCopy = ((W.length - i * dig.length) > dig.length)
                    ? dig.length
                    : (W.length - i * dig.length);
            System.arraycopy(dig, 0, W, i * dig.length, bytesToCopy);

            addTo(data, ONE);
        }

        return W;
    }

    private static class WorkingBuffer
    {
        private byte[] _V;
        private byte[] _C;

        @Override
        protected void finalize() throws Throwable
        {
            super.finalize();

            Arrays.fill(_V, (byte) 0);
            Arrays.fill(_C, (byte) 0);
        }
    }
}

