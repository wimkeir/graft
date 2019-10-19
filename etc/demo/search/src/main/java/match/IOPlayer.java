package match;

import java.util.List;
import java.util.Scanner;

import domains.MNK;

/**
 * An MNK player that gets the next move from user input.
 */
public class IOPlayer extends BasePlayer {

    public IOPlayer(int side) {
        super(side);
    }

    @Override
    public MNK play(MNK board) {
        Scanner in = new Scanner(System.in);
        List<Integer> legalMoves = board.getMoves();

        int move;
        System.out.println("Choose a move:");
        move = in.nextInt();
        while (!legalMoves.contains(move)) {
            System.out.println("Illegal move, please choose another:");
            move = in.nextInt();
        }

        return (MNK) board.makeMove(move);
    }

}
