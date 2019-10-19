package transposition;

import org.junit.Test;

import domains.GameTree;
import domains.MNK;

import static org.junit.Assert.*;

public class TestTransTableTWODEEP {

    @Test
    public void testIsEmptyAndReset() {
        TranspositionTable table = new TransTableTWODEEP(10);
        assertTrue(table.isEmpty());

        GameTree entry = new MNK(1, 2, 3, 4);
        table.put(1, entry, 1, 4, 20);
        assertFalse(table.isEmpty());

        table.reset();
        assertTrue(table.isEmpty());
    }

    @Test
    public void testNrEntries() {
        TranspositionTable table = new TransTableTWODEEP(10);
        assertEquals(0, table.nrEntries());

        GameTree entry = new MNK(1, 2, 3, 4);
        table.put(1, entry, 1, 4, 20);
        assertEquals(1, table.nrEntries());

        // insert entry with different key
        table.put((long) Math.pow(2, 55), entry, 1, 4, 20);
        assertEquals(2, table.nrEntries());

        // insert entry with same hash key, different hash value
        table.put(2, entry, 1, 4, 20);
        assertEquals(3, table.nrEntries());

        // insert entry with same key (should overwrite)
        table.put(2, entry, 1, 4, 20);
        assertEquals(3, table.nrEntries());
    }

    @Test
    public void testContainsKey() {
        TranspositionTable table = new TransTableTWODEEP(10);
        assertFalse(table.containsKey(1));

        GameTree entry = new MNK(1, 2, 3, 4);
        table.put(1, entry, 1, 4, 20);
        assertTrue(table.containsKey(1));

        // insert entry with same hash key, different hash value
        table.put(2, entry, 1, 4, 20);
        assertTrue(table.containsKey(2));

        table.reset();
        assertFalse(table.containsKey(1));
    }

    @Test
    public void testGet() {
        TranspositionTable table = new TransTableTWODEEP(10);
        GameTree entry = new MNK(1, 2, 3, 4);

        assertNull(table.get(1));
        table.put(1, entry, 1, 4, 20);
        assertEquals(4, table.get(1).player);
    }

    @Test
    public void testReplacementScheme() {
        TranspositionTable table = new TransTableTWODEEP(10);

        GameTree entry = new MNK(1, 2, 3, 5);
        GameTree shallower = new MNK(2, 3, 4, 1);
        GameTree deeper = new MNK(3, 4, 5, 2);

        long entryKey = 1L;
        long shallowKey = 2L;
        long deepKey = 3L;

        assertTrue(table.put(entryKey, entry, 1, 4, 20));
        assertTrue(table.containsKey(entryKey));
        assertEquals(5, table.get(entryKey).player);

        assertTrue(table.put(shallowKey, shallower, 1, 3, 20));
        assertTrue(table.containsKey(shallowKey));
        assertTrue(table.containsKey(entryKey));
        assertEquals(5, table.get(entryKey).player);
        assertEquals(1, table.get(shallowKey).player);

        assertTrue(table.put(deepKey, deeper, 1, 5, 20));
        assertTrue(table.containsKey(deepKey));
        assertTrue(table.containsKey(entryKey));
        assertEquals(2, table.get(deepKey).player);
        assertEquals(5, table.get(entryKey).player);
    }
}
