package transposition;

import domains.GameTree;

import static utils.BitwiseOps.*;

/**
 * A one-level transposition table implementation with the NEW replacement scheme.
 */
public class TransTableNEW extends OneLevelTransTable {

    /**
     * Initialize an empty transposition table with the NEW replacement scheme.
     *
     * @param size the size of the transposition table
     */
    public TransTableNEW(int size) {
        super(size);
    }

    @Override
    public boolean put(long key, GameTree entry) {
        int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
        if (hashKey < 0 || hashKey >= TABLE_SIZE) {
            System.out.println("Key outside table bounds: " + key);
            return false;
        }
        long hashVal = getLastKBits(key, HASHVAL_SIZE);

        // always replace the old entry with the new one
        if (!containsKey(hashKey)) {
            nrEntries++;
        }
        entries[hashKey] = new TableEntry(entry, hashVal);
        return true;
    }

}