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
     * @param hashkeySize the size of the transposition table
     */
    public TransTableDEEP(int hashkeySize) {
        super(hashkeySize);
    }

    @Override
    public boolean put(long key, GameTree entry, int bestMove, int searchDepth, int score) {
        return put(key, entry, bestMove, searchDepth, score, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    @Override
    public boolean put(long key,
                       GameTree entry,
                       int bestMove,
                       int searchDepth,
                       int score,
                       int lowerBound,
                       int upperBound) {
        int hashKey = (int) getFirstKBits(key, HASHKEY_SIZE);
        if (hashKey < 0 || hashKey >= TABLE_SIZE) {
            System.out.println("Key outside table bounds: " + key);
            return false;
        }
        long hashVal = getLastKBits(key, HASHVAL_SIZE);

        // if the key isn't in the table, there's nothing to replace, we just add a new entry
        if (entries[hashKey] == null) {
            nrEntries++;
            entries[hashKey] = new TableEntry(entry.getPlayer(),
                    searchDepth,
                    bestMove,
                    hashVal,
                    score,
                    lowerBound,
                    upperBound);
            return true;
        }

        // otherwise, only replace the old entry if the new entry has an equal or greater depth
        if (entries[hashKey].searchDepth <= searchDepth) {
            entries[hashKey] = new TableEntry(entry.getPlayer(),
                    searchDepth,
                    bestMove,
                    hashVal,
                    score,
                    lowerBound,
                    upperBound);
            return true;
        }

        return false;
    }
}
