package transposition;

import domains.GameTree;

import static utils.BitwiseOps.*;

/**
 * A two-level transposition table implementation with the TWODEEP replacement scheme.
 */
public class TransTableTWODEEP extends TwoLevelTransTable {

    /**
     * Initialize an empty transposition table with the TWODEEP replacement scheme.
     *
     * @param size the size of the transposition table
     */
    public TransTableTWODEEP(int size) {
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

        // if the key isn't in the table, place the entry in the first position
        if (!containsKey(hashKey)) {
            entries[hashKey].first = new InnerEntry(entry, hashVal);
            nrEntries++;
            return true;
        }

        // if the depth is greater than the entry in the first position, then we
        // move the first entry to the second position and place the new entry in
        // the first position
        if (entries[hashKey].first.gameTree.searchDepth() <= entry.searchDepth()) {
            entries[hashKey].second = entries[hashKey].first;
            entries[hashKey].first = new InnerEntry(entry, hashVal);
            return true;
        }

        // otherwise, always replace the second entry with the newest entry
        entries[hashKey].second = new InnerEntry(entry, hashVal);
        return true;
    }

}
