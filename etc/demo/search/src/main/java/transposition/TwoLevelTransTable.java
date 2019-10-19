package transposition;

import static utils.BitwiseOps.*;

/**
 * A base implementation of a fixed-size, two-level transposition table.
 * 
 * This should be subclassed to implement specific replacement schemes.
 */
public abstract class TwoLevelTransTable extends BaseTransTable {

    protected OuterEntry[] entries;

    /**
     * Initialize an empty two-level transposition table.
     */
    TwoLevelTransTable(int hashkeySize) {
        super(hashkeySize);
        nrEntries = 0;
        entries = new OuterEntry[TABLE_SIZE];
    }

    // ************************************************************************
    // implemented TransTable methods
    // ************************************************************************

    @Override
    public TableEntry get(long key) {
        int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
        if (hashKey < 0 || hashKey >= TABLE_SIZE) {
            System.out.println("Key outside table bounds: " + key);
            return null;
        }
        long hashVal = getLastKBits(key, HASHVAL_SIZE);

        // if the outer entry doesn't exist, we don't check any further
        if (entries[hashKey] == null) {
            return null;
        }

        // otherwise we check if the first or second entries match the hash value
        if (entries[hashKey].first != null && entries[hashKey].first.hashVal == hashVal) {
            return entries[hashKey].first;
        }
        if (entries[hashKey].second != null && entries[hashKey].second.hashVal == hashVal) {
            return entries[hashKey].second;
        }

        return null;
    }

    @Override
    public boolean containsKey(long key) {
        int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
        if (hashKey < 0 || hashKey >= TABLE_SIZE) {
            System.out.println("Key outside table bounds: " + key);
            return false;
        }
        long hashVal = getLastKBits(key, HASHVAL_SIZE);

        // check that we actually have an entry for this key
        if (entries[hashKey] == null) {
            return false;
        }

        // otherwise we check if the first or second entries match the hash value
        if (entries[hashKey].first != null &&
            entries[hashKey].first.hashVal == hashVal) {
            return true;
        }

        return entries[hashKey].second != null &&
               entries[hashKey].second.hashVal == hashVal;
    }

    @Override
    public void reset() {
        entries = new OuterEntry[TABLE_SIZE];
        nrEntries = 0;
    }

    // ************************************************************************
    // private classes
    // ************************************************************************

    /**
     * An outer container for two-layered entries in the transposition table.
     */
    protected static class OuterEntry {

        TableEntry first;
        TableEntry second;

    }
}