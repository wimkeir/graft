package algorithms;

import domains.GameTree;
import domains.MNK;
import transposition.*;

/**
 * An implementation of the MTD(f) algorithm, using iterative deepening alpha beta search.
 */
public class MTDf implements Algorithm {

    private int depthCutoff;
    private long startTime;
    private long endTime;
    private IterativeDeepeningAlphaBetaMNK ab;

    // ************************************************************************
    // constructors
    // ************************************************************************

    public MTDf(int depthCutoff, ReplacementScheme repScheme, int timeCutoff) {
        this.depthCutoff = depthCutoff;
        ab = new IterativeDeepeningAlphaBetaMNK(depthCutoff, repScheme, timeCutoff, true);
    }

    // ************************************************************************
    // implemented Algorithm methods
    // ************************************************************************

    @Override
    public int getBestMove(GameTree tree) {
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;

        startTime = System.currentTimeMillis();
        for (int move : tree.getMoves()) {
            int firstGuess = 0;
            for (int d = 0; d <= depthCutoff; d += 2) {
                firstGuess = mtdf(tree.makeMove(move), firstGuess, d);
            }
            if (firstGuess > bestScore) {
                bestMove = move;
                bestScore = firstGuess;
            }
        }
        endTime = System.currentTimeMillis();

        return bestMove;
    }

    @Override
    public int getTranspTableHits() {
        return ab.getTranspTableHits();
    }

    @Override
    public int getTranspTableMisses() {
        return ab.getTranspTableMisses();
    }

    @Override
    public int getTranspTableSize() {
        return ab.getTranspTableSize();
    }

    @Override
    public int getNodesExplored() {
        return ab.getNodesExplored();
    }

    @Override
    public long getElapsedTime() {
        return endTime - startTime;
    }

    @Override
    public void resetStats() {
        ab.resetStats();
    }

    // ************************************************************************
    // implemented Algorithm methods
    // ************************************************************************

    private int mtdf(GameTree tree, int f, int depth) {
        int g = f;
        int upper = Integer.MAX_VALUE;
        int lower = Integer.MIN_VALUE + 1;

        while (lower < upper) {
            int beta = Math.max(g, lower + 1);
            g = ab.alphaBetaWithTrans((MNK) tree, beta - 1, beta, depth, true);
            if (g < beta) {
                upper = g;
            } else {
                lower = g;
            }
        }
        return g;
    }

}
