package algorithms;

import domains.DigitsOfPi;
import domains.GameTree;

import java.util.List;

/**
 * Implementation of the F algorithm from Knuth and Moore.
 */
public class F implements PiAlgorithm, Algorithm {

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
        for (int move : successors) {
            GameTree child = tree.makeMove(move);
            int tmp = -fAlgo(child, -1);
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
        DigitsOfPi node = new DigitsOfPi(depth);
        fAlgo(node, 1);
        node.show();
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    /**
     * Main algorithm for the implementation is a recursive depth first search.
     *
     * @param p the current game tree i.e. node
     * @param player min or max player
     * @return returns the evaluation value
     */
    private int fAlgo(GameTree p, int player) {
        nodesExplored++;
        int m, t;
        if (p.isTerminal()) {
            return player * p.getEvaluation();
        } else {
            List<Integer> successors = p.getMoves();
            m = Integer.MIN_VALUE;
            for (int pi : successors) {
                t = -fAlgo(p.makeMove(pi), -player);
                if (t > m) {
                    m = t;
                }
            }
            return m;
        }
    }

}
