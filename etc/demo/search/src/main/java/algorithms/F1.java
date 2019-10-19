package algorithms;

import domains.DigitsOfPi;
import domains.GameTree;

import java.util.List;

/**
 * Implementation of the F1 algorithm from Knuth and Moore.
 */
public class F1 implements Algorithm, PiAlgorithm {

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
        int bestScore = Integer.MIN_VALUE + 1;

        startTime = System.currentTimeMillis();
        for (int move : successors) {
            GameTree child = tree.makeMove(move);
            int tmp = f1Algo(child, -Integer.MAX_VALUE, -1);
            if (tmp > bestScore) {
                bestScore = tmp;
                bestMove = move;
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
        f1Algo(root, Integer.MAX_VALUE, 1);
        root.show();
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    /**
     * Main algorithm for the implementation, a recursive depth first search with a lower bound.
     *
     * @param p current game tree
     * @param bound current upper bound
     * @return the best evaluation found
     */
    private int f1Algo(GameTree p, int bound, int player) {
        nodesExplored++;
        int m, t;
        if (p.isTerminal()) {
            return player * p.getEvaluation();
        } else {
            List<Integer> successors = p.getMoves();
            m = Integer.MIN_VALUE + 1;
            for (int pi : successors) {
                t = -f1Algo(p.makeMove(pi), -m, -player);
                if (t > m) m = t;
                if (m >= bound) {
                    return m;
                }
            }
            return m;
        }
    }

}
