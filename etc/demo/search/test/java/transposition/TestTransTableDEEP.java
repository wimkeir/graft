package transposition;

import org.junit.Test;

import domains.GameTree;
import domains.MNK;


import static org.junit.Assert.*;

public class TestTransTableDEEP {

//    /**
//     * Test that inserting an entry with a smaller depth than the existing entry does not replace
//     * it, but one with an equal or larger depth does.
//     */
//    @Test
//    public void testReplacementScheme() {
//        TranspositionTable table = new TransTableDEEP(10);
//
//        GameTree first = new MNK(1, 1, 1, 1, 2);
//        GameTree smaller = new MNK(1, 1, 1, 1, 1);
//        GameTree equal = new MNK(1, 1, 1, 1, 2);
//        GameTree greater = new MNK(1, 1, 1, 1, 3);
//
//        // insert the first entry
//        assertTrue(table.put(1, first));
//        assertTrue(table.containsKey(1));
//        assertEquals(first, table.get(1));
//
//        // insert the entry with smaller depth (should not replace first)
//        assertFalse(table.put(1, smaller));
//        assertTrue(table.containsKey(1));
//        assertEquals(first, table.get(1));
//
//        // insert the entry with equal depth (should replace first)
//        assertTrue(table.put(1, equal));
//        assertTrue(table.containsKey(1));
//        assertEquals(equal, table.get(1));
//
//        // insert the entry with greater depth (should replace equal)
//        assertTrue(table.put(1, greater));
//        assertTrue(table.containsKey(1));
//        assertEquals(greater, table.get(1));
//    }

}
