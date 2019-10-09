package transposition;

import domains.GameTree;

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
    public GameTree get(long key) {
        int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
        if (hashKey < 0 || hashKey >= TABLE_SIZE) {
            System.out.println("Key outside table bounds: " + key);
            return null;
        }

        long hashVal = getLastKBits(key, HASHVAL_SIZE);
        if (entries[hashKey].hashVal == hashVal) {
            return entries[hashKey].gameTree;
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

    // ************************************************************************
    // abstract methods (these depend on the replacement scheme)
    // ************************************************************************

    @Override
    public abstract boolean put(long key, GameTree entry);

    // ************************************************************************
    // private classes
    // ************************************************************************

    /**
     * A container for entries in the transposition table.
     */
    protected static class TableEntry {

        GameTree gameTree;
        long hashVal;

        public TableEntry(GameTree gameTree, long hashVal) {
            this.gameTree = gameTree;
            this.hashVal = hashVal;
        }

    }

}