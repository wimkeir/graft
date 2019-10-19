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
     * @param hashkeySize the size of the transposition table
     */
    public TransTableTWODEEP(int hashkeySize) {
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

        // if no outer entry exists for this key, create one
        if (entries[hashKey] == null) {
            entries[hashKey] = new OuterEntry();
        }

        // if no deepest entry exists, we add  the new entry in there
        if (entries[hashKey].first == null) {
            // we should never have a newer entry without having a deeper entry
            assert entries[hashKey].second == null;
            nrEntries++;
            entries[hashKey].first = new TableEntry(entry.getPlayer(),
                    searchDepth,
                    bestMove,
                    hashVal,
                    score,
                    lowerBound,
                    upperBound);
            return true;
        }

        // if the depth is greater than the entry in the first position, then we
        // move the first entry to the second position and place the new entry in
        // the first position
        if (entries[hashKey].first.searchDepth <= searchDepth) {
            // the second entry might not exist
            if (entries[hashKey].second == null) {
                nrEntries++;
            }
            entries[hashKey].second = entries[hashKey].first;
            entries[hashKey].first = new TableEntry(entry.getPlayer(),
                    searchDepth,
                    bestMove,
                    hashVal,
                    score,
                    lowerBound,
                    upperBound);
            return true;
        }

        // otherwise, always replace the second entry with the newest entry
        entries[hashKey].second = new TableEntry(entry.getPlayer(),
                searchDepth,
                bestMove,
                hashVal,
                score,
                lowerBound,
                upperBound);
        return true;
    }

}
