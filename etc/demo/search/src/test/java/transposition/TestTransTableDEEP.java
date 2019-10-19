package transposition;

import org.junit.Test;

import domains.GameTree;
import domains.MNK;

import static org.junit.Assert.*;

public class TestTransTableDEEP {

    @Test
    public void testIsEmptyAndReset() {
        TranspositionTable table = new TransTableDEEP(10);
        assertTrue(table.isEmpty());

        GameTree entry = new MNK(1, 2, 3, 5);
        table.put(1, entry, 1, 3, 20);
        assertFalse(table.isEmpty());

        table.reset();
        assertTrue(table.isEmpty());
    }

    @Test
    public void testNrEntries() {
        TranspositionTable table = new TransTableDEEP(10);
        assertEquals(0, table.nrEntries());

        GameTree entry = new MNK(1, 2, 3, 5);
        table.put(1, entry, 1, 3, 20);
        assertEquals(1, table.nrEntries());

        // insert entry with different key
        table.put((2L << 55), entry, 1, 3, 20);
        assertEquals(2, table.nrEntries());

        // insert entry with same key (should overwrite)
        table.put(2, entry, 1, 3, 20);
        assertEquals(2, table.nrEntries());
    }

    @Test
    public void testContainsKey() {
        TranspositionTable table = new TransTableDEEP(10);
        assertFalse(table.containsKey(1));

        GameTree entry = new MNK(1, 2, 3, 5);
        table.put(1, entry, 1, 3, 20);
        assertTrue(table.containsKey(1));

        table.reset();
        assertFalse(table.containsKey(1));
    }

    @Test
    public void testGet() {
        TranspositionTable table = new TransTableDEEP(10);
        GameTree entry = new MNK(1, 2, 3, 5);

        assertNull(table.get(1));
        table.put(1, entry, 1, 3, 20);
        assertEquals(5, table.get(1).player);
    }

    @Test
    public void testReplacementScheme() {
        TranspositionTable table = new TransTableDEEP(10);

        GameTree entry = new MNK(1, 2, 3, 5);
        GameTree shallower = new MNK(2, 3, 4, 1);
        GameTree deeper = new MNK(3, 4, 5, 2);

        long entryKey = 1;
        long shallowKey = 2;
        long deepKey = 3;

        // shallow entries should not overwrite
        assertTrue(table.put(entryKey, entry, 1, 4, 20));
        assertFalse(table.put(shallowKey, shallower, 1, 3, 20));
        assertEquals(5, table.get(entryKey).player);

        // deeper entries should overwrite
        assertTrue(table.put(deepKey, deeper, 1, 5, 20));
        assertFalse(table.containsKey(entryKey));
        assertEquals(2, table.get(deepKey).player);
    }

}
