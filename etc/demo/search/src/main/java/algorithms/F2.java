package algorithms;

import domains.DigitsOfPi;
import domains.GameTree;

import java.util.List;

/**
 * Implementation of the F2 algorithm from Knuth and Moore.
 */
public class F2 implements Algorithm, PiAlgorithm {

    private long startTime;
    private long endTime;
    private int nodesExplored;

    // ************************************************************************
    // implemented Algorithm methods
    // ************************************************************************

    @Override
    public int getBestMove(GameTree tree) {
        List<Integer> successors = tree.getMoves();
        int bestMove = -1;
        int bestScore = Integer.MIN_VALUE;

        startTime = System.currentTimeMillis();
        for (int c : successors) {
            if (-f2Algo(tree, -Integer.MAX_VALUE, Integer.MAX_VALUE) > bestScore) {
                bestMove = c;
            }
        }
        endTime = System.currentTimeMillis();
        return bestMove;
    }

    @Override
    public int getTranspTableHits() {
        return 0;
    }

    @Override
    public int getTranspTableMisses() {
        return 0;
    }

    @Override
    public int getTranspTableSize() {
        return 0;
    }

    @Override
    public int getNodesExplored() {
        return nodesExplored;
    }

    @Override
    public long getElapsedTime() {
        return endTime - startTime;
    }

    @Override
    public void resetStats() {

    }

    // ************************************************************************
    // implemented PiAlgorithm methods
    // ************************************************************************

    @Override
    public void evaluate(int depth) {
        DigitsOfPi root = new DigitsOfPi(depth);
        f2Algo(root, -10, 10);
        root.show();
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    /**
     * Main algorithm for the implementation is a recursive depth first search with an upper and lower bound.
     *
     * @param p current game tree
     * @param alpha current upper bound
     * @param beta current lower bound
     * @return the best evaluation found
     */
    private int f2Algo(GameTree p, int alpha, int beta) {
        nodesExplored++;
        int m, t;
        if (p.isTerminal()) {
            return p.getEvaluation();
        } else {
            m = alpha;
            List<Integer> successors = p.getMoves();
            for (int c : successors) {
                t = -f2Algo(p.makeMove(c), -beta, -m);
                if (t > m) m = t;
                if (m >= beta) return m;
            }
            return m;
        }
    }
}
