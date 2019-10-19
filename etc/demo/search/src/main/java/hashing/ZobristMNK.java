package hashing;

import domains.MNK;

import java.util.Arrays;
import java.util.Random;

/**
 * A Zobrist hashing utility for MNK boards.
 */
public class ZobristMNK {

    // dimensions of the board
    private int M, N;

    // move hashes for each player
    private long[] xMoveHashes;
    private long[] oMoveHashes;

    // ************************************************************************
    // constructors
    // ************************************************************************

    /**
     * Initializes a new Zobrist hashing class for the given MNK board dimensions.
     *
     * @param m the width of the board
     * @param n the height of the board
     */
    public ZobristMNK(int m, int n) {
        this.M = m;
        this.N = n;
        do {
            initTables();
        } while (!tablesUnique());
    }

    // ************************************************************************
    // public methods
    // ************************************************************************

    /**
     * Calculates the Zobrist hash value of the given MNK board.
     *
     * @param board the board to hash
     * @return the Zobrist hash of the board
     */
    public long getHash(MNK board) {
        long hash = 0;
        for (int x = 0; x < board.getWidth(); x++) {
            for (int y = 0; y < board.getHeight(); y++) {
                if (board.getPos(x, y) == MNK.X) {
                    hash ^= getHash(x, y, MNK.X);
                } else if (board.getPos(x, y) == MNK.O) {
                    hash ^= getHash(x, y, MNK.O);
                }
            }
        }
        return hash;
    }

    /**
     * Get the hash of a given MNK move.
     *
     * @param x the x-position of the move
     * @param y the y-position of the move
     * @param player the player making the move
     * @return the hash of the move
     */
    public long getHash(int x, int y, int player) {
        assert player == MNK.X || player == MNK.O;
        if (player == MNK.X) {
            return xMoveHashes[linearize(x, y)];
        } else {
            return oMoveHashes[linearize(x, y)];
        }
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    // fills the tables with random longs for each possible move
    private void initTables() {
        xMoveHashes = new long[M*N];
        oMoveHashes = new long[M*N];
        Random rand = new Random();

        for (int i = 0; i < M*N; i++) {
            xMoveHashes[i] = rand.nextLong();
            oMoveHashes[i] = rand.nextLong();
        }
    }

    // checks whether each move hash is unique
    private boolean tablesUnique() {
        Arrays.sort(xMoveHashes);
        Arrays.sort(oMoveHashes);
        for (int i = 0; i < M*N - 1; i++) {
            if (xMoveHashes[i] == xMoveHashes[i + 1] ||
                oMoveHashes[i] == oMoveHashes[i + 1] ||
                xMoveHashes[i] == oMoveHashes[i]) {
                return false;
            }
        }
        return true;
    }

    // linearizes a given position on the board
    private int linearize(int x, int y) {
        return N * y + x;
    }

}
