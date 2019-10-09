package transposition;

import domains.GameTree;

import static utils.BitwiseOps.*;

/**
 * A one-level transposition table implementation with the DEEP replacement scheme.
 */
public class TransTableDEEP extends OneLevelTransTable {

    /**
     * Initialize an empty transposition table with the DEEP replacement scheme.
     *
     * @param size the size of the transposition table
     */
    public TransTableDEEP(int size) {
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

        // if the key isn't in the table, there's nothing to replace
        if (!containsKey(key)) {
            nrEntries++;
            entries[hashKey] = new TableEntry(entry, hashVal);
            return true;
        }

        // only replace the old entry if the new entry has an equal or greater depth
        if (entries[hashKey].gameTree.searchDepth() <= entry.searchDepth()) {
            entries[hashKey] = new TableEntry(entry, hashVal);
            return true;
        }

        return false;
    }
}
