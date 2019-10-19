package utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class TestBitwiseOps {

    @Test
    public void testGetFirstKBits() {
        int x = 185;
        assertEquals(11, BitwiseOps.getFirstKBits(x, 60));
    }

    @Test
    public void testGetLastKBits() {
        int x = 185;
        assertEquals(9, BitwiseOps.getLastKBits(x, 4));
    }

}
