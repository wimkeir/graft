package algorithms;

import domains.GameTree;
import domains.MNK;
import hashing.ZobristMNK;
import transposition.*;

import java.util.ArrayList;
import java.util.List;

public class NegaAlphaBeta implements Algorithm {

    private TranspositionTable table;
    private long startTime;
    private long endTime;
    private int depth;
    private int numNodesExplored = 0;
    private int transpTableHits = 0;
    private int transpTableMisses = 0;

    // ************************************************************************
    // private methods
    // ************************************************************************

    public NegaAlphaBeta(int depth) {
        this.depth = depth;
        table = null;
    }

    public NegaAlphaBeta(int depth, ReplacementScheme scheme) {
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
            score = -negamax(child, depth - 1, Integer.MIN_VALUE + 1, -alpha, -1);
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
            return -1;
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
        transpTableHits = 0;
        transpTableMisses = 0;
        numNodesExplored = 0;
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    private int negamax(GameTree node, int depth, int alpha, int beta, int player) {
        numNodesExplored++;
        TableEntry tte = null;
        int score = Integer.MIN_VALUE + 1;
        if (depth == 0 || node.isTerminal()) {
            return player * node.getEvaluation();
        } else if (node instanceof MNK && table != null) {
            long hash = node.getHash();
            tte = table.get(hash);
            if (table.containsKey(hash)) {
                transpTableHits ++;
            } else {
                transpTableMisses ++;
            }
            if (tte != null && tte.searchDepth >= depth) {
                return tte.player == ((MNK)node).MAX ? tte.score : -tte.score;
            }
        }

        List<Integer> moves = node.getMoves();
        int move = moves.get(0);
        if (tte != null) {
            int bestMove = tte.bestMove;
            GameTree child = node.makeMove(bestMove);
            score = -negamax(child, depth - 1, -beta, -alpha, -player);
            if (score > alpha) {
                alpha = score;
                move = bestMove;
            }
            if (alpha >= beta) {
                return alpha;
            }
        }
        for (int m : moves) {
            if (tte != null && m == tte.bestMove) continue;
            score = Math.max(score, -negamax(node.makeMove(m), depth - 1, -beta, -alpha, -player));
            if (score > alpha) {
                alpha = score;
                move = m;
            }
            if (alpha >= beta) {
                break;
            }
        }
        if (table != null && node instanceof MNK) {
            long hash = node.getHash();
            table.put(hash, node, move, depth, alpha);
        }
        return alpha;

    }
}
