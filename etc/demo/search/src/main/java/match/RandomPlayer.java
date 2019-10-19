package match;

import java.util.List;
import java.util.Random;

import domains.MNK;

/**
 * An MNK adversary that chooses its next move at random.
 */
public class RandomPlayer extends BasePlayer {

    public RandomPlayer(int side) {
        super(side);
    }

    @Override
    public MNK play(MNK board) {
        List<Integer> legalMoves = board.getMoves();
        Random rand = new Random();
        int move = rand.nextInt(legalMoves.size());
        return (MNK) board.makeMove(legalMoves.get(move));
    }

}
