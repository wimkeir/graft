package algorithms;

import domains.GameTree;
import domains.MNK;
import hashing.ZobristMNK;
import transposition.*;

import java.util.List;

/**
 * Iterative deepening alpha beta search with or without a transposition table
 */
public class IterativeDeepeningAlphaBetaMNK implements Algorithm {

    private static int POS_INF = Integer.MAX_VALUE;
    private static int NEG_INF = -Integer.MAX_VALUE + 1;

    private long startTime;
    private long endTime;
    private int numNodesExplored;
    private long start;
    private double timeThreshold;
    private boolean allLeaf = true;
    private boolean useTable = true;
    private int maxDepth;
    private int bestMove;
    private int transHit;
    private int transMiss;
    private boolean timesUp = false;
    private TranspositionTable transpositionTable;

    // ************************************************************************
    // constructors
    // ************************************************************************

    /**
     * Initialize a new iterative alpha-beta MNK algorithm.
     *
     * @param maxDepth max depth to search to
     * @param scheme the replacement scheme to use
     * @param timeThreshold the max amount of time allowed for the search before cut-off
     */
    public IterativeDeepeningAlphaBetaMNK(int maxDepth, ReplacementScheme scheme, int timeThreshold) {
        this.maxDepth = maxDepth;
        this.timeThreshold = timeThreshold;
        switch (scheme) {
            case TWO_DEEP:
                this.transpositionTable = new TransTableTWODEEP(16);
                break;
            case NEW:
                this.transpositionTable = new TransTableNEW(16);
                break;
            case DEEP:
                this.transpositionTable = new TransTableDEEP(16);
                break;
            case NONE:
                useTable = false;
                this.transpositionTable = null;
        }
    }

    /**
     * Initialize a new iterative alpha-beta MNK algorithm.
     *
     * @param maxDepth max depth to search to
     * @param scheme the replacement scheme to use
     * @param timeThreshold the max amount of time allowed for the search before cut-off
     */
    public IterativeDeepeningAlphaBetaMNK(int maxDepth, ReplacementScheme scheme, int timeThreshold, boolean fromMtdf) {
        this.maxDepth = maxDepth;
        this.timeThreshold = timeThreshold;
        switch (scheme) {
            case TWO_DEEP:
                this.transpositionTable = new TransTableTWODEEP(16);
                break;
            case NEW:
                this.transpositionTable = new TransTableNEW(16);
                break;
            case DEEP:
                this.transpositionTable = new TransTableDEEP(16);
                break;
            case NONE:
                useTable = false;
                this.transpositionTable = null;
        }
        if (fromMtdf) {
            this.start = System.currentTimeMillis();
        }
    }

    // ************************************************************************
    // implemented Algorithm methods
    // ************************************************************************

    @Override
    public int getBestMove(GameTree tree) {
        List<Integer> children = tree.getMoves();
        timeThreshold = timeThreshold / children.size();
        int curBest = NEG_INF;
        int bestMove = -1;

        startTime = System.currentTimeMillis();
        for (int m : children) {
            int eval = searchFromRoot(tree.makeMove(m), false);
            if (eval > curBest) {
                curBest = eval;
                bestMove = m;
            }
        }
        endTime = System.currentTimeMillis();
        // reset values
        allLeaf = true;
        timesUp = false;
        return bestMove;
    }

    @Override
    public int getTranspTableHits() {
        return transHit;
    }

    @Override
    public int getTranspTableMisses() {
        return transMiss;
    }

    @Override
    public int getTranspTableSize() {
        if (transpositionTable != null) {
            return transpositionTable.nrEntries();
        } else {
            return 0;
        }
    }

    @Override
    public int getNodesExplored() {
        return numNodesExplored;
    }

    @Override
    public long getElapsedTime() {
        return endTime - startTime;
    }

    @Override
    public void resetStats() {
        numNodesExplored = 0;
        transHit = 0;
        transMiss = 0;
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    /**
     * Start an alpha beta search from the given root
     * @param root the game tree to start the search from
     * @param maxPlayer min or max player
     * @return the best evaluation found in the search
     */
    private int searchFromRoot(GameTree root, boolean maxPlayer) {
        numNodesExplored++;
        int result = 0;
        long hesh = root.getHash();

        this.start = System.currentTimeMillis();
        for (int i = 0; i <= maxDepth; i += 2) {
            int prevBestMove = bestMove;
            if (useTable) {
                result = alphaBetaWithTrans((MNK) root, NEG_INF, POS_INF, i, maxPlayer);

                // add root if not already in
                if (!transpositionTable.containsKey(hesh)) {
                    transMiss++;
                    transpositionTable.put(hesh, root, bestMove, i, result);
                } else {
                    transHit++;
                }
            } else {
                result = alphaBeta((MNK) root, NEG_INF, POS_INF, i, maxPlayer);
            }

            if (timesUp) {
                bestMove = prevBestMove;
                break;
            } else if (allLeaf) {
                break;
            }

            allLeaf = true;
        }

        return result;
    }

    /**
     * Recursive iterative deepening alpha beta search with a transposition table set in searchFromRoot.
     * Source: https://en.wikipedia.org/wiki/Alpha%E2%80%93beta_pruning
     *
     * @param board the current board
     * @param alpha the current alpha value at this depth
     * @param beta the current beta value at this depth
     * @param depth the current depth from root
     * @param isMax min or ma player
     * @return returns the best evaluation found
     */
    public int alphaBetaWithTrans(MNK board, int alpha, int beta, int depth, boolean isMax) {
        numNodesExplored++;
        int v;
        double elapsedTime = (System.currentTimeMillis() - start) / 1000F;

        if (elapsedTime > timeThreshold) {
            timesUp = true;
            return board.getEvaluation();
        }

        if (board.isLeaf() || depth == 0) {
            allLeaf = allLeaf && board.isLeaf();
            return board.getEvaluation();
        }

        if (board.isWon()) {
            return board.getEvaluation();
        } else {
            if (isMax) { // MAX
                v = NEG_INF;
                List<Integer> successors = board.getMoves();

                long hash = board.getHash();
                int curBestMove = -1;

                if (transpositionTable.containsKey(hash)) {
                    transHit++;
                    curBestMove = transpositionTable.get(hash).bestMove;
                    MNK nextBoard = (MNK) board.makeMove(curBestMove);
                    int ret = alphaBetaWithTrans(nextBoard, alpha, beta, depth - 1, false);
                    v = Math.max(v, ret);
                    alpha = Math.max(alpha, v);
                } else {
                    transMiss++;
                }

                for (int c : successors) {
                    if (c == curBestMove) continue;
                    int ret = alphaBetaWithTrans((MNK) board.makeMove(c), alpha, beta, depth - 1, false);
                    if (ret > v) bestMove = c;

                    v = Math.max(v, ret);
                    alpha = Math.max(alpha, v);

                    if (alpha >= beta | timesUp) {
                        break; // beta cut-off
                    }
                }

                transpositionTable.put(hash, board, bestMove, depth, v);
                return v;

            } else { // MIN

                v = POS_INF;
                List<Integer> successors = board.getMoves();
                long hash = board.getHash();
                int curBestMove = -1;

                if (transpositionTable.containsKey(hash)) {
                    transHit++;
                    curBestMove = transpositionTable.get(hash).bestMove;
                    MNK nextBoard = (MNK) board.makeMove(curBestMove);
                    int ret = alphaBetaWithTrans(nextBoard, alpha, beta, depth - 1, true);

                    v = Math.min(v, ret);
                    beta = Math.min(beta, v);
                } else {
                    transMiss++;
                }

                for (int c : successors) {
                    if (c == curBestMove) continue;
                    int ret = alphaBetaWithTrans((MNK) board.makeMove(c), alpha, beta, depth - 1, true);
                    if (ret > v) bestMove = c;

                    v = Math.min(v, ret);
                    beta = Math.min(beta, v);

                    if (alpha >= beta || timesUp) {
                        break; // alpha cut-off
                    }
                }
                transpositionTable.put(hash, board, bestMove, depth, board.getEvaluation());
                return v;
            }
        }
    }

    /**
     * Recursive iterative deepening alpha beta search without a transposition table.
     *
     * @param board the current board
     * @param alpha the current alpha value at this depth
     * @param beta the current beta value at this depth
     * @param depth the current depth from root
     * @param isMax min or ma player
     * @return returns the best evaluation found
     */
    private int alphaBeta(MNK board, int alpha, int beta, int depth, boolean isMax) {
        numNodesExplored++;
        int v;
        double elapsedTime = (System.currentTimeMillis() - start) / 1000F;
        if (elapsedTime > timeThreshold) {
            timesUp = true;
            System.out.println("Out of time returning...");
            return board.getEvaluation();
        }

        if (board.isLeaf() || depth == 0) {
            allLeaf = allLeaf && board.isLeaf();
            return board.getEvaluation();
        }
        if (board.isWon()) {
            return board.getEvaluation();
        } else {
            if (isMax) { // MAX
                v = NEG_INF;
                List<Integer> successors = board.getMoves();
                for (int c : successors) {
                    int ret = alphaBeta((MNK) board.makeMove(c), alpha, beta, depth - 1, false);
                    v = Math.max(v, ret);
                    alpha = Math.max(alpha, v);

                    if (alpha >= beta | timesUp) {
                        break; // beta cut-off
                    }
                }
                return v;

            } else { // MIN

                v = POS_INF;
                List<Integer> successors = board.getMoves();
                for (int c : successors) {
                    int ret = alphaBeta((MNK) board.makeMove(c), alpha, beta, depth - 1, true);
                    v = Math.min(v, ret);
                    beta = Math.min(beta, v);

                    if (alpha >= beta | timesUp) {
                        break; // alpha cut-off
                    }
                }
                return v;
            }
        }
    }

}
