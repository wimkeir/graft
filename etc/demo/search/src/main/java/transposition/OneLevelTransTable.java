package transposition;

import static utils.BitwiseOps.*;

/**
 * A base implementation of a fixed-size, one-level transposition table.
 * 
 * This should be subclassed to implement specific replacement schemes.
 */
public abstract class OneLevelTransTable extends BaseTransTable {

    /**
     * A fixed-size array of table entries, indexed by hash values.
     */
    protected TableEntry[] entries;

    // ************************************************************************
    // constructors
    // ************************************************************************

    /**
     * Initialize an empty one-level transposition table.
     */
    OneLevelTransTable(int hashkeySize) {
        super(hashkeySize);
        nrEntries = 0;
        entries = new TableEntry[TABLE_SIZE];
    }

    // ************************************************************************
    // implemented TransTable methods
    // ************************************************************************

    @Override
    public TableEntry get(long key) {
        if (containsKey(key)) {
            int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
            return entries[hashKey];
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
        return entries[hashKey] != null && entries[hashKey].hashVal == hashVal;
    }

    @Override
    public void reset() {
        entries = new TableEntry[TABLE_SIZE];
        nrEntries = 0;
    }

}