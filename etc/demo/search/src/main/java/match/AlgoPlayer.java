package match;

import algorithms.Algorithm;
import domains.MNK;

/**
 * An MNK adversary that uses the given minimax algorithm to select the next move.
 */
public class AlgoPlayer extends BasePlayer {

    private Algorithm algo;

    public AlgoPlayer(int side, Algorithm algo) {
        super(side);
        this.algo = algo;
    }

    @Override
    public MNK play(MNK board) {
        int move = algo.getBestMove(board);
        // display stats
        System.out.println("Elapsed time: " + algo.getElapsedTime());
        System.out.println("Nodes explored: " + algo.getNodesExplored());
        System.out.println("Transp. table hits: " + algo.getTranspTableHits());
        System.out.println("Transp. table misses: " + algo.getTranspTableMisses());
        System.out.println("Transp. table size: " + algo.getTranspTableSize());
        algo.resetStats();
        assert move != -1;
        return (MNK) board.makeMove(move);
    }

}
