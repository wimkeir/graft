package match;

import domains.MNK;

/**
 * A driver class for hosting MNK matches between two adversaries.
 */
public class MNKMatch {

    private MNKPlayer player1;
    private MNKPlayer player2;
    private MNK currentBoard;

    // ************************************************************************
    // constructors
    // ************************************************************************

    /**
     * Initialize a new match with an empty board.
     *
     * @param player1 the first player
     * @param player2 the second player
     * @param m the width of the board
     * @param n the height of the board
     * @param k matchable length
     */
    public MNKMatch(MNKPlayer player1, MNKPlayer player2, int m, int n, int k) {
        assert player1.getSide() != player2.getSide();
        this.player1 = player1;
        this.player2 = player2;
        currentBoard = new MNK(m, n, k, player1.getSide());
    }

    /**
     * Initialize a new match with the given board state.
     *
     * @param player1 the first player
     * @param player2 the second player
     * @param initialBoard the initial board state
     */
    public MNKMatch(MNKPlayer player1, MNKPlayer player2, MNK initialBoard) {
        assert player1.getSide() != player2.getSide();
        assert player1.getSide() == initialBoard.getPlayer();
        this.player1 = player1;
        this.player2 = player2;
        currentBoard = initialBoard;
    }

    // ************************************************************************
    // public methods
    // ************************************************************************

    /**
     * Play the match until one player wins or the game is drawn.
     */
    public void play() {
        // TODO: keep board history
        currentBoard.draw();
        while (true) {
            System.out.println("Player 1 playing");
            currentBoard.setMAX(player1.getSide());
            currentBoard = player1.play(currentBoard);
            currentBoard.draw();
            if (currentBoard.isWon()) {
                System.out.println("Player 1 wins!");
                break;
            } else if (currentBoard.getMoves().isEmpty()) {
                System.out.println("Draw!");
                break;
            }

            System.out.println("Player 2 playing");
            currentBoard.setMAX(player2.getSide());
            currentBoard = player2.play(currentBoard);
            currentBoard.draw();
            if (currentBoard.isWon()) {
                System.out.println("Player 2 wins!");
                break;
            } else if (currentBoard.getMoves().isEmpty()) {
                System.out.println("Draw!");
                break;
            }
        }
    }
}
