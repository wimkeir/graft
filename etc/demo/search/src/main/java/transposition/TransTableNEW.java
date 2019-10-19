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
     * @param hashkeySize the size of the transposition table
     */
    public TransTableNEW(int hashkeySize) {
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

        // if no entry for this key exists, we create one
        if (entries[hashKey] == null) {
            nrEntries++;
        }

        // always replace the old entry with the new one
        entries[hashKey] = new TableEntry(entry.getPlayer(),
                searchDepth,
                bestMove,
                hashVal,
                score);
        return true;    }

}