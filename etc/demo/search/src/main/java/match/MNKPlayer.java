package match;

import domains.MNK;

/**
 * An interface for an adversary in the MNK domain.
 */
public interface MNKPlayer {

    /**
     * Given a board state, find the next move and play it.
     *
     * @param board the board to play on
     * @return the board resulting from the move
     */
    MNK play(MNK board);

    /**
     * Get the "side" of the player (ie. X or O).
     *
     * @return either MNK.X or MNK.O
     */
    int getSide();

}
