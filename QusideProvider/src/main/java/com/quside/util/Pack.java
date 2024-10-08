package com.quside.util;

/**
 * Utility methods for converting byte arrays into ints and longs, and back again.
 */
public class Pack
{

    private Pack(){}

    public static int bigEndianToInt(byte[] bs, int off)
    {
        int n = bs[off] << 24;
        n |= (bs[++off] & 0xff) << 16;
        n |= (bs[++off] & 0xff) << 8;
        n |= (bs[++off] & 0xff);
        return n;
    }


    public static void intToBigEndian(int n, byte[] bs, int off)
    {
        bs[off] = (byte) (n >>> 24);
        bs[++off] = (byte) (n >>> 16);
        bs[++off] = (byte) (n >>> 8);
        bs[++off] = (byte) (n);
    }


    public static long bigEndianToLong(byte[] bs, int off)
    {
        int hi = bigEndianToInt(bs, off);
        int lo = bigEndianToInt(bs, off + 4);
        return ((long) (hi & 0xffffffffL) << 32) | (long) (lo & 0xffffffffL);
    }


    public static byte[] longToBigEndian(long n)
    {
        byte[] bs = new byte[8];
        longToBigEndian(n, bs, 0);
        return bs;
    }

    public static void longToBigEndian(long n, byte[] bs, int off)
    {
        intToBigEndian((int) (n >>> 32), bs, off);
        intToBigEndian((int) (n & 0xffffffffL), bs, off + 4);
    }
}
