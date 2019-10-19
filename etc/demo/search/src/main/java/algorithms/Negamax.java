package algorithms;

import domains.GameTree;

import java.util.List;

public class Negamax implements Algorithm {

    private int depth;
    private int numExplored;
    private long startTime;
    private long endTime;

    // ************************************************************************
    // implemented Algorithm methods
    // ************************************************************************

    @Override
    public int getBestMove(GameTree tree) {
        int score = Integer.MIN_VALUE + 1;
        int bestMove = -1;
        List<Integer> moves = tree.getMoves();

        startTime = System.currentTimeMillis();
        for (int m : moves) {
            int tmp = -negamax(tree.makeMove(m), depth - 1, -1);
            if (score < tmp) {
                score = tmp;
                bestMove = m;
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
        return numExplored;
    }

    @Override
    public long getElapsedTime() {
        return endTime - startTime;
    }

    @Override
    public void resetStats() {
        numExplored = 0;
    }

    public Negamax(int depth) {
        this.depth = depth;
        numExplored = 0;
    }

    // ************************************************************************
    // implemented Algorithm methods
    // ************************************************************************

    private int negamax(GameTree node, int depth, int player) {
        if (depth == 0 || node.isTerminal()) {
            return player * node.getEvaluation(depth);
        } else {
            int score = Integer.MIN_VALUE + 1;
            List<Integer> moves = node.getMoves();
            for (int m : moves) {
                score = Math.max(score, -negamax(node.makeMove(m), depth - 1, -player));
            }
            return score;
        }
    }

}
