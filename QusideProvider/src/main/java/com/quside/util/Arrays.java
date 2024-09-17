package com.quside.util;

/**
 * General array utilities.
 */
public final class Arrays
{
    private Arrays()
    {
        // static class, hide constructor
    }

    public static boolean areEqual(byte[] a, byte[] b)
    {
        return java.util.Arrays.equals(a, b);
    }

    /**
     * A constant time equals comparison - does not terminate early if
     * test will fail. For best results always pass the expected value
     * as the first parameter.
     *
     * @param expected first array
     * @param supplied second array
     * @return true if arrays equal, false otherwise.
     */
    public static boolean constantTimeAreEqual(
            byte[] expected,
            byte[] supplied)
    {
        if (expected == null || supplied == null)
        {
            return false;
        }

        if (expected == supplied)
        {
            return true;
        }

        int len = (expected.length < supplied.length) ? expected.length : supplied.length;

        int nonEqual = expected.length ^ supplied.length;

        for (int i = 0; i != len; i++)
        {
            nonEqual |= (expected[i] ^ supplied[i]);
        }
        for (int i = len; i < supplied.length; i++)
        {
            nonEqual |= (supplied[i] ^ ~supplied[i]);
        }

        return nonEqual == 0;
    }


    public static void fill(byte[] a, byte val)
    {
        java.util.Arrays.fill(a, val);
    }

    /**
     * @deprecated Use {@link #fill(byte[], int, int, byte)} instead.
     */
    public static void fill(byte[] a, int fromIndex, byte val)
    {
        fill(a, fromIndex, a.length, val);
    }

    public static void fill(byte[] a, int fromIndex, int toIndex, byte val)
    {
        java.util.Arrays.fill(a, fromIndex, toIndex, val);
    }

    public static byte[] clone(byte[] data)
    {
        return null == data ? null : data.clone();
    }


    /**
     * Make a copy of a range of bytes from the passed in array. The range can extend beyond the end
     * of the input array, in which case the returned array will be padded with zeroes.
     *
     * @param original the array from which the data is to be copied.
     * @param from     the start index at which the copying should take place.
     * @param to       the final index of the range (exclusive).
     * @return a new byte array containing the range given.
     */
    public static byte[] copyOfRange(byte[] original, int from, int to)
    {
        int newLength = getLength(from, to);
        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
        return copy;
    }

    private static int getLength(int from, int to)
    {
        int newLength = to - from;
        if (newLength < 0)
        {
            StringBuffer sb = new StringBuffer(from);
            sb.append(" > ").append(to);
            throw new IllegalArgumentException(sb.toString());
        }
        return newLength;
    }


    public static byte[] concatenate(byte[] a, byte[] b)
    {
        if (null == a)
        {
            // b might also be null
            return clone(b);
        }
        if (null == b)
        {
            // a might also be null
            return clone(a);
        }

        byte[] r = new byte[a.length + b.length];
        System.arraycopy(a, 0, r, 0, a.length);
        System.arraycopy(b, 0, r, a.length, b.length);
        return r;
    }


    public static byte[] concatenate(byte[] a, byte[] b, byte[] c)
    {
        if (null == a)
        {
            return concatenate(b, c);
        }
        if (null == b)
        {
            return concatenate(a, c);
        }
        if (null == c)
        {
            return concatenate(a, b);
        }

        byte[] r = new byte[a.length + b.length + c.length];
        int pos = 0;
        System.arraycopy(a, 0, r, pos, a.length);
        pos += a.length;
        System.arraycopy(b, 0, r, pos, b.length);
        pos += b.length;
        System.arraycopy(c, 0, r, pos, c.length);
        return r;
    }

    public static byte[] concatenate(byte[] a, byte[] b, byte[] c, byte[] d)
    {
        if (null == a)
        {
            return concatenate(b, c, d);
        }
        if (null == b)
        {
            return concatenate(a, c, d);
        }
        if (null == c)
        {
            return concatenate(a, b, d);
        }
        if (null == d)
        {
            return concatenate(a, b, c);
        }

        byte[] r = new byte[a.length + b.length + c.length + d.length];
        int pos = 0;
        System.arraycopy(a, 0, r, pos, a.length);
        pos += a.length;
        System.arraycopy(b, 0, r, pos, b.length);
        pos += b.length;
        System.arraycopy(c, 0, r, pos, c.length);
        pos += c.length;
        System.arraycopy(d, 0, r, pos, d.length);
        return r;
    }

}

