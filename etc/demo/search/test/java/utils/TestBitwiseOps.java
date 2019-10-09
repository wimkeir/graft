package utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestBitwiseOps {

    @Test
    public void testGetFirstKBits() {
        int x = 185;    // 10111001, we expect 00001011
        assertEquals(11, BitwiseOps.getFirstKBits(x, 4));
    }

    @Test
    public void testGetLastKBits() {
        int x = 185;    // 10111001, we expect 00001001
        assertEquals(9, BitwiseOps.getLastKBits(x, 4));
    }

}
