package transposition;

import domains.GameTree;

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
    public GameTree get(long key) {
        int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
        if (hashKey < 0 || hashKey >= TABLE_SIZE) {
            System.out.println("Key outside table bounds: " + key);
            return null;
        }
        long hashVal = getLastKBits(key, HASHVAL_SIZE);

        if (entries[hashKey].first != null && entries[hashKey].first.hashVal == hashVal) {
            return entries[hashKey].first.gameTree;
        }
        if (entries[hashKey].second != null && entries[hashKey].second.hashVal == hashVal) {
            return entries[hashKey].second.gameTree;
        }

        return null;
    }

    public boolean containsKey(long key) {
        int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
        if (hashKey < 0 || hashKey >= TABLE_SIZE) {
            System.out.println("Key outside table bounds: " + key);
            return false;
        }
        long hashVal = getLastKBits(key, HASHVAL_SIZE);

        if (entries[hashKey].first != null && entries[hashKey].first.hashVal == hashVal) {
            return true;
        }
        if (entries[hashKey].second != null && entries[hashKey].second.hashVal == hashVal) {
            return true;
        }

        return false;
    }

    @Override
    public void reset() {
        entries = new OuterEntry[TABLE_SIZE];
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
     * An outer container for two-layered entries in the transposition table.
     */
    protected static class OuterEntry {

        InnerEntry first;
        InnerEntry second;

        public OuterEntry(InnerEntry first, InnerEntry second) {
            this.first = first;
            this.second = second;
        }

    }

    /**
     * An inner container for the actual entries in the transposition table.
     */
    protected static class InnerEntry {

        GameTree gameTree;
        long hashVal;

        public InnerEntry(GameTree gameTree, long hashVal) {
            this.gameTree = gameTree;
            this.hashVal = hashVal;
        }

    }

}