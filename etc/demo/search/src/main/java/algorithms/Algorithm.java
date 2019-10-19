package algorithms;

import domains.GameTree;

/**
 * Common interface for search algorithms.
 */
public interface Algorithm {

    /**
     * Get the recommended best move from the given state.
     *
     * @param tree the root of the game tree to search
     * @return the number of the child to choose as the best move
     */
    int getBestMove(GameTree tree);

    /**
     * Get the number of transposition table hits during the search.
     *
     * @return the number of table hits
     */
    int getTranspTableHits();

    /**
     * Get the number of transposition table misses during the search.
     *
     * @return the number of table misses
     */
    int getTranspTableMisses();

    /**
     * Get the number of entries in the transposition table.
     *
     * @return the size of the table
     */
    int getTranspTableSize();

    /**
     * Get the number of nodes of the game tree explored.
     *
     * @return the number of nodes explored
     */
    int getNodesExplored();

    /**
     * Get the duration of the latest search.
     *
     * @return the duration of the search
     */
    long getElapsedTime();

    /**
     * Reset all stats for the next algorithm run.
     */
    void resetStats();

}
