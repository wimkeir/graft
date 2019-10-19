package transposition;

import org.junit.Test;

import domains.GameTree;
import domains.MNK;

import static org.junit.Assert.*;

public class TestTransTableNEW {

    @Test
    public void testIsEmptyAndReset() {
        TranspositionTable table = new TransTableNEW(10);
        assertTrue(table.isEmpty());

        GameTree entry = new MNK(1, 2, 3, 5);
        table.put(1, entry, 1, 3, 20);
        assertFalse(table.isEmpty());

        table.reset();
        assertTrue(table.isEmpty());
    }

    @Test
    public void testNrEntries() {
        TranspositionTable table = new TransTableNEW(10);
        assertEquals(0, table.nrEntries());

        GameTree entry = new MNK(1, 2, 3,5);
        table.put(1, entry, 1, 4, 20);
        assertEquals(1, table.nrEntries());

        // insert entry with different key
        table.put((2L << 55), entry, 1, 4, 20);
        assertEquals(2, table.nrEntries());

        // insert entry with same key (should overwrite)
        table.put(2, entry, 1, 4, 20);
        assertEquals(2, table.nrEntries());
    }

    @Test
    public void testContainsKey() {
        TranspositionTable table = new TransTableNEW(10);
        assertFalse(table.containsKey(1));

        GameTree entry = new MNK(1, 2, 3, 4);
        table.put(1, entry, 1, 4, 20);
        assertTrue(table.containsKey(1));

        table.reset();
        assertFalse(table.containsKey(1));
    }

    @Test
    public void testGet() {
        TranspositionTable table = new TransTableNEW(10);
        GameTree entry = new MNK(1, 2, 3, 4);

        assertNull(table.get(1));
        table.put(1, entry, 1, 4,20);
        assertEquals(4, table.get(1).player);
    }

    @Test
    public void testReplacementScheme() {
        TranspositionTable table = new TransTableNEW(10);

        GameTree entry = new MNK(1, 2, 3, 4);
        GameTree newer = new MNK(2, 3, 4, 3);

        long entryKey = 1;
        long newKey = 2;

        assertTrue(table.put(entryKey, entry, 1, 4, 5));
        assertTrue(table.containsKey(entryKey));
        assertEquals(4, table.get(entryKey).player);

        // newer entry should always overwrite
        assertTrue(table.put(newKey, newer, 1, 4, 1));
        assertTrue(table.containsKey(newKey));
        assertFalse(table.containsKey(entryKey));
        assertEquals(3, table.get(newKey).player);
    }

}