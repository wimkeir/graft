package algorithms;

import domains.GameTree;
import domains.MNK;
import transposition.*;

import java.util.ArrayList;

public class NegaScout implements Algorithm {

    private final int depth;
    private final TranspositionTable table;
    private int transpTableHits = 0;
    private int transpTableMisses = 0;
    private int numNodesExplored = 0;
    private long startTime;
    private long endTime;

    // ************************************************************************
    // constructors
    // ************************************************************************

    public NegaScout(int depth) {
        this.depth = depth;
        this.table = null;
    }

    public NegaScout(int depth, ReplacementScheme scheme) {
        this.depth = depth;
        switch (scheme) {
            case DEEP:
                table = new TransTableDEEP(15);
                break;
            case NEW:
                table = new TransTableNEW(15);
                break;
            case TWO_DEEP:
                table = new TransTableTWODEEP(15);
                break;
            default:
                throw new RuntimeException("Bad replacement scheme");
        }
    }

    // ************************************************************************
    // implemented Algorithm methods
    // ************************************************************************

    @Override
    public int getBestMove(GameTree tree) {
        // Wraps around scout to return best move
        ArrayList<Integer> moves = (ArrayList<Integer>) tree.getMoves();
        int alpha = Integer.MIN_VALUE + 1;
        if (tree.isTerminal() || depth <= 0) {
            return -1;
        }
        int bestMove = moves.get(0);

        startTime = System.currentTimeMillis();
        for (int m = 0; m < moves.size(); m++) {
            int score;
            GameTree child = tree.makeMove(moves.get(m));
            if (m == 0) {
                score = -scout(child, Integer.MIN_VALUE + 1, -alpha, -1, depth - 1);
            } else {
                score = -scout(child, -alpha - 1, -alpha, -1, depth - 1);
                if (score > alpha && score < Integer.MAX_VALUE) {
                    score = -scout(child, Integer.MIN_VALUE + 1, -alpha, -1, depth - 1);
                }
            }
            if (score > alpha) {
                alpha = score;
                bestMove = moves.get(m);
            }
        }
        endTime = System.currentTimeMillis();

        return bestMove;
    }

    @Override
    public int getTranspTableHits() {
        return transpTableHits;
    }

    @Override
    public int getTranspTableMisses() {
        return transpTableMisses;
    }

    @Override
    public int getTranspTableSize() {
        if (table != null) {
            return table.nrEntries();
        } else {
            return 0;
        }
    }

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
        transpTableHits = 0;
        transpTableMisses = 0;
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    private int scout(GameTree node, int alpha, int beta, int player, int depth) {
        numNodesExplored ++;
        int move = -1;
        TableEntry tte = null;
        if (node instanceof MNK && table != null) {
            long hash = node.getHash();
            if (table.containsKey(hash)) {
                transpTableHits++;
            } else {
                transpTableMisses++;
            }
            tte = table.get(hash);
            if (tte != null && tte.searchDepth > depth) {
                return tte.player == ((MNK)node).MAX ? tte.score : -tte.score;
            }
        }

        int score;
        ArrayList<Integer> moves = (ArrayList<Integer>) node.getMoves();
        if (node.isTerminal() || depth <= 0) {
            return player * node.getEvaluation(depth + 1);
        }
        if (tte != null) {
            int bestMove = tte.bestMove;
            GameTree child = node.makeMove(bestMove);
            score = -scout(child, -beta, -alpha, -player, depth - 1);
            if (score > alpha) {
                alpha = score;
                move = bestMove;
            }
        }
        for (int m = 0; m < moves.size(); m++) {
            if (tte != null && moves.get(m) == tte.bestMove) {
                continue;
            }
            GameTree child = node.makeMove(moves.get(m));
            if (tte == null && m == 0) {
                score = -scout(child, -beta, -alpha, -player, depth - 1);
            } else {
                score = -scout(child, -alpha - 1, -alpha, -player, depth - 1);
                if (score > alpha && score < beta) {
                    score = -scout(child, -beta, -alpha, -player, depth - 1);
                }
            }
            if (score > alpha) {
                alpha = score;
                move = moves.get(m);
            }
            if (alpha >= beta) {
                break;
            }
        }

        if (node instanceof MNK && table != null) {
            long hash = node.getHash();
            if (move == -1) move = 0;
            table.put(hash, node, move, depth, alpha);
        }
        return alpha;
    }

}
