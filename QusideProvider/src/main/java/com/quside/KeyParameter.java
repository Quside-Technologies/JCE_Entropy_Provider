package com.quside;


import com.quside.util.Arrays;

class KeyParameter

{
    private byte[]  key;

    public KeyParameter(
            byte[] key)
    {
        this.key = key;
    }

    public KeyParameter(byte[] keyvalue, int offSet, int length)
    {
        this.key = Arrays.copyOfRange(keyvalue, offSet, offSet + length);
    }

    public byte[] getKey()
    {
        return key;
    }
}
