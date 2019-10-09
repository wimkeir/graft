package hashing;

import domains.MNK;

import java.util.Random;

/**
 * TODO: test this somehow
 * TODO: javadoc
 */
public class Zobrist {

    private long[][] zobristTable;

    public Zobrist(int m, int n) {
        initTable(m*n, 2);
    }

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
                    hash ^= zobristTable[x*y][0];
                } else if (board.getPos(x, y) == MNK.O) {
                    hash ^= zobristTable[x*y][1];
                }
            }
        }
        return hash;
    }

    // fills a table with random longs for each possible move in the domain
    private void initTable(int nrPos, int nrPieces) {
        zobristTable = new long[nrPos][nrPieces];
        Random rand = new Random();

        for (int x = 0; x < nrPos; x++) {
            for (int y = 0; y < nrPieces; y++) {
                zobristTable[x][y] = rand.nextLong();
            }
        }
    }

}
