package transposition;

import domains.GameTree;

/**
 * An interface for transposition table implementations.
 */
public interface TranspositionTable {

    /**
     * Get the table entry for the given key, if it exists.
     * 
     * @param key the key to look up
     * @return the table entry associated with the key, or null if it doesn't exist
     */
    GameTree get(long key);

    /**
     * Insert an entry into the table with the given key.
     * 
     * Returns false if the entry could not be inserted (ie. the replacement strategy does not
     * allow the new entry to replace the current entry).
     * 
     * @param key the key to associate the entry with
     * @param val the game tree to store
     * @return true if the entry was added, else false
     */
    boolean put(long key, GameTree val);

    /**
     * Checks whether the table contains an entry for the given key.
     * 
     * @param key the key to check for membership
     * @return true if the table contains a value for the key, else false
     */
    boolean containsKey(long key);

    /**
     * Get the number of entries in the table.
     * 
     * @return the number of entries
     */
    int nrEntries();

    /**
     * Check if the table is empty.
     * 
     * @return true if the table is empty, else false
     */
    boolean isEmpty();

    /**
     * Clear the table and reset internal data structures.
     */
    void reset();

}