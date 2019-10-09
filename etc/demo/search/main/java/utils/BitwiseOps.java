package utils;

import transposition.BaseTransTable;

public class BitwiseOps {

    /**
     * Returns the first k bits of the given long.
     *
     * @param x the long to extract the bits from
     * @param k the number of bits to extract
     * @return the first k bits of the long
     */
    public static long getFirstKBits(long x, int k) {
        return x >>> k;
    }

    /**
     * Returns the last k bits of the given long.
     *
     * @param x the long to extract the bits from
     * @param k the number of bits to extract
     * @return the last k bits of the long
     */
    public static long getLastKBits(long x, int k) {
        return (0xFFFFFFFFFFFFFFFFL >>> (BaseTransTable.HASH_SIZE - k)) & x;
    }

}
