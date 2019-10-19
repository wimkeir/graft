package transposition;

/**
 * An entry in the transposition table.
 */
public class TableEntry {

    public int player;
    public int searchDepth;
    public int bestMove;
    public int lowerBound;
    public int upperBound;
    public long hashVal;
    public int score;

    /**
     * Initialise a new transposition table entry.
     *
     * @param player the player whose perspective the entry was evaluated from
     * @param searchDepth the depth to which the entry was searched
     * @param bestMove the best move to take from the entry
     * @param hashVal the hash value of the entry
     * @param score the evaluation score of the entry
     * @param lowerBound the lower bound of the node
     * @param upperBound the upper bound of the node
     */
    public TableEntry(int player,
                      int searchDepth,
                      int bestMove,
                      long hashVal,
                      int score,
                      int lowerBound,
                      int upperBound) {
        this.player = player;
        this.searchDepth = searchDepth;
        this.bestMove = bestMove;
        this.hashVal = hashVal;
        this.score = score;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    /**
     * Initialise a new transposition table entry.
     *
     * @param player the player whose perspective the entry was evaluated from
     * @param searchDepth the depth to which the entry was searched
     * @param bestMove the best move to take from the entry
     * @param hashVal the hash value of the entry
     * @param score the evaluation score of the entry
     */
    public TableEntry(int player,
                      int searchDepth,
                      int bestMove,
                      long hashVal,
                      int score) {
        this.player = player;
        this.searchDepth = searchDepth;
        this.bestMove = bestMove;
        this.hashVal = hashVal;
        this.score = score;
    }

}
