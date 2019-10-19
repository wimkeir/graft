package domains;

import hashing.ZobristMNK;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * An implementation of the (m, n, k) game environment.
 */
public class MNK implements GameTree {

    public static int X = 1;
    public static int L = 3;
    public static int O = -1;

    // the value of a win in the heuristic
    private static int WIN = 1000;

    private boolean won = false;
    public int MAX;
    private int[] grid;
    private int width, height, k, player;
    private int evaluation;
    private int treeDepth;

    private final ZobristMNK hasher;
    private final long hash;

    // ************************************************************************
    // constructors
    // ************************************************************************

    /**
     * Initialise an empty MNK board.
     *
     * @param m the height of the board
     * @param n the width of the board
     * @param k the number of pieces in a match
     * @param player the perspective of the board
     */
    public MNK(int m, int n, int k, int player) {
        this.hasher = new ZobristMNK(m, n);
        this.hash = 0;
        MAX = player;
        this.width = m;
        this.height = n;
        this.k = k;
        this.grid = new int[m * n];
        this.player = player;
        evaluation = 0;
        this.treeDepth = 0;
    }

    /**
     * Initialise an MNK board with a given state.
     *
     * @param grid the board state to use
     * @param k the number of pieces to match
     * @param player the perspective of the board
     */
    public MNK(int[][] grid, int k, int player) {
        MAX = player;
        this.height = grid.length;
        this.width = grid[0].length;
        this.hasher = new ZobristMNK(height, width);
        int c = 0;
        this.grid = new int[height * width];
        for (int[] i : grid) {
            for (int j : i) {
                assert j >= -1 && j <= 1;
                this.grid[c++] = j;
            }
        }
        this.player = player;
        this.k = k;
        this.evaluation = initEval();
        this.treeDepth = 0;
        this.hash = hasher.getHash(this);

    }

    /**
     * Private constructor to generate children of a board.
     *
     * @param grid the state to use
     * @param m the height of the board
     * @param k the number of pieces to match
     * @param player the perspective of the parent board
     * @param move the move being applied
     * @param oldEval the evaluation of the parent
     * @param treeDepth the depth of the tree node
     * @param MAX the player to maximise
     */
    private MNK(int[] grid, int m, int k, int player, int move, int oldEval, int treeDepth, int MAX, long hash, ZobristMNK hasher) {
        this.hasher = hasher;
        this.grid = grid;
        this.width = m;
        this.height = grid.length / m;
        this.k = k;
        this.player = player;
        this.MAX = MAX;
        this.evaluation = evaluate(oldEval, move);
        this.treeDepth = treeDepth;
        this.hash = hash ^ hasher.getHash(getCol(move), getRow(move), -player);
    }


    // ************************************************************************
    // implemented GameTree methods
    // ************************************************************************

    @Override
    public List<Integer> getMoves() {
        List<Integer> l = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            if (grid[i] == 0) {
                l.add(i);
            }
        }
        return l;
    }

    @Override
    public GameTree makeMove(Integer i) {
        GameTree child;
        int[] childGrid = grid.clone();
        childGrid[i] = player;
        child = new MNK(childGrid, width, k, -player, i, -evaluation, treeDepth + 1, MAX, hash, hasher);
        return child;
    }

    @Override
    public int getEvaluation() {
        return evaluation;
    }

    @Override
    public int getEvaluation(int depth) {
        return depth * evaluation;
    }

    @Override
    public int getPlayer() {
        return player;
    }

    @Override
    public int hashFunction() {
        return Arrays.hashCode(grid);
    }

    @Override
    public void draw() {
        int c = 0;
        for (int i : grid) {
            if (c++ == width) {
                System.out.println();
                c = 1;
            }
            String player = (i == 1) ? "X" : ((i == -1) ? "O" : "_");
            System.out.print(player + " ");
        }
        System.out.println();
    }

    @Override
    public boolean isTerminal() {
        return won || this.getMoves().isEmpty();
    }

    @Override
    public long getHash() {
        return this.hash;
    }


    // ************************************************************************
    // public utility methods
    // ************************************************************************

    /**
     * Get the player at the given position.
     *
     * @param x the x-value of the position
     * @param y the y-value of the position
     * @return the piece at that position
     */
    public int getPos(int x, int y) {
        int pos = linearise(x, y);
        return grid[pos];
    }

    /**
     * Get the height of the board.
     *
     * @return the height of the board
     */
    public int getHeight() {
        return height;
    }

    /**
     * Get the width of the board.
     *
     * @return the width of the board
     */
    public int getWidth() {
        return width;
    }

    /**
     * Get the maximising player.
     *
     * @param MAX the maximising player
     */
    public void setMAX(int MAX) {
        this.MAX = MAX;
    }

    /**
     * Get the number of pieces to match.
     *
     * @return the number of pieces to match
     */
    public int getK() {
        return k;
    }

    // ************************************************************************
    // helper methods
    // ************************************************************************

    private int linearise(int col, int row) {
        return width * row + col;
    }

    private int getCol(int r) {
        return r % width;
    }

    private int getRow(int r) {
        return r / width;
    }

    private int checkThreats() {
        return countPieces(MAX) - countPieces(-MAX);
    }

    private int checkNewThreats(int move) {
        int val = 0;
        int tmp = grid[move];
        grid[move] = 0;
        val -= pieceCounter(grid, move);
        grid[move] = tmp;
        val += pieceCounter(grid, move);
        return val;
    }

    private int countPieces(int player) {
        int val = 0;
        int numPieces;
        int counter;
        // columns
        for (int col = 0; col < width; col++) {
            numPieces = 0;
            counter = 0;
            for (int row = 0; row < height; row++) {
                if (grid[linearise(col, row)] == -player) {
                    if (counter >= k) val += numPieces;
                    numPieces = 0;
                    counter = 0;
                } else if (grid[linearise(col, row)] == player) {
                    numPieces++;
                    counter++;
                } else {
                    counter++;
                }
            }
            if (counter >= k) val += numPieces;
        }

        // rows
        for (int row = 0; row < height; row++) {
            numPieces = 0;
            counter = 0;
            for (int col = 0; col < width; col++) {
                if (grid[linearise(col, row)] == -player) {
                    if (counter >= k) val += numPieces;
                    numPieces = 0;
                    counter = 0;
                } else if (grid[linearise(col, row)] == player) {
                    numPieces++;
                    counter++;
                } else {
                    counter++;
                }
            }
            if (counter >= k) val += numPieces;
        }

        // diag tl-br
        for (int col = 0; col <= width - k; col++) {
            numPieces = 0;
            counter = 0;
            for (int tmpCol = col, row = 0; tmpCol < width && row < height; tmpCol++, row++) {
                if (grid[linearise(tmpCol, row)] == -player) {
                    if (counter >= k) val += numPieces;
                    numPieces = 0;
                    counter = 0;
                } else if (grid[linearise(tmpCol, row)] == player) {
                    numPieces++;
                    counter++;
                } else {
                    counter++;
                }
            }
            if (counter >= k) val += numPieces;
        }
        for (int row = 1; row <= height - k; row++) {
            numPieces = 0;
            counter = 0;
            for (int tmpRow = row, col = 0; col < width && tmpRow < height; tmpRow++, col++) {
                if (grid[linearise(col, tmpRow)] == -player) {
                    if (counter >= k) val += numPieces;
                    numPieces = 0;
                    counter = 0;
                } else if (grid[linearise(col, tmpRow)] == player) {
                    numPieces++;
                    counter++;
                } else {
                    counter++;
                }
            }
            if (counter >= k) val += numPieces;
        }

        // diag tr-bl
        // cols
        for (int col = k - 1; col < width; col++) {
            numPieces = 0;
            counter = 0;
            for (int tmpCol = col, row = 0; tmpCol >= 0 && row < height; tmpCol--, row++) {
                if (grid[linearise(tmpCol, row)] == -player) {
                    if (counter >= k) val += numPieces;
                    numPieces = 0;
                    counter = 0;
                } else if (grid[linearise(tmpCol, row)] == player) {
                    numPieces++;
                    counter++;
                } else {
                    counter++;
                }
            }
            if (counter >= k) val += numPieces;
        }

        // rows
        for (int row = 1; row <= height - k; row++) {
            numPieces = 0;
            counter = 0;
            for (int tmpRow = row, col = width - 1; col >= 0 && tmpRow < height; tmpRow++, col--) {
                if (grid[linearise(col, tmpRow)] == -player) {
                    if (counter >= k) val += numPieces;
                    numPieces = 0;
                    counter = 0;
                } else if (grid[linearise(col, tmpRow)] == player) {
                    numPieces++;
                    counter++;
                } else {
                    counter++;
                }
            }
            if (counter >= k) val += numPieces;
        }

        return val;
    }

    private int checkWin() {
        // horiz
        int ret = 0;
        int counterP;
        int counterNP;

        for (int row = 0; row < height; row++) {
            counterP = 0;
            counterNP = 0;
            for (int col = 0; col < width; col++) {
                if (grid[linearise(col, row)] == MAX) {
                    counterP++;
                    counterNP = 0;
                    if (counterP == k) {
                        ret += WIN;
                        won = true;
                    }
                } else if (grid[linearise(col, row)] == -MAX) {
                    counterNP++;
                    counterP = 0;
                    if (counterNP == k) {
                        ret -= WIN;
                        won = true;
                    }
                } else {
                    counterP = 0;
                    counterNP = 0;
                }
            }
        }

        // vert
        for (int col = 0; col < width; col++) {
            counterP = 0;
            counterNP = 0;
            for (int row = 0; row < height; row++) {
                if (grid[linearise(col, row)] == MAX) {
                    counterP++;
                    counterNP = 0;
                    if (counterP == k) {
                        ret += WIN;
                        won = true;
                    }
                } else if (grid[linearise(col, row)] == -MAX) {
                    counterNP++;
                    counterP = 0;
                    if (counterNP == k) {
                        ret -= WIN;
                        won = true;
                    }
                } else {
                    counterP = 0;
                    counterNP = 0;
                }
            }
        }

        // diag tl - br
        // cols
        for (int col = 0; col <= width - k; col++) {
            counterP = 0;
            counterNP = 0;
            for (int tmpCol = col, row = 0; tmpCol < width && row < height; tmpCol++, row++) {
                if (grid[linearise(tmpCol, row)] == MAX) {
                    counterP++;
                    counterNP = 0;
                    if (counterP == k) {
                        won = true;
                        ret += WIN;
                    }
                } else if (grid[linearise(tmpCol, row)] == -MAX) {
                    counterNP++;
                    counterP = 0;
                    if (counterNP == k) {
                        won = true;
                        ret -= WIN;
                    }
                } else {
                    counterP = 0;
                    counterNP = 0;
                }
            }
        }
        // rows
        for (int row = 1; row <= height - k; row++) {
            counterP = 0;
            counterNP = 0;
            for (int tmpRow = row, col = 0; col < width && tmpRow < height; tmpRow++, col++) {
                if (grid[linearise(col, tmpRow)] == MAX) {
                    counterP++;
                    counterNP = 0;
                    if (counterP == k) {
                        won = true;
                        ret += WIN;
                    }
                } else if (grid[linearise(col, tmpRow)] == -MAX) {
                    counterNP++;
                    counterP = 0;
                    if (counterNP == k) {
                        won = true;
                        ret -= WIN;
                    }
                } else {
                    counterP = 0;
                    counterNP = 0;
                }
            }
        }

        // diag tr - bl
        // cols
        for (int col = k - 1; col < width; col++) {
            counterP = 0;
            counterNP = 0;
            for (int tmpCol = col, row = 0; tmpCol >= 0 && row < height; tmpCol--, row++) {
                if (grid[linearise(tmpCol, row)] == MAX) {
                    counterP++;
                    counterNP = 0;
                    if (counterP == k) {
                        won = true;
                        ret += WIN;
                    }
                } else if (grid[linearise(tmpCol, row)] == -MAX) {
                    counterNP++;
                    counterP = 0;
                    if (counterNP == k) {
                        won = true;
                        ret -= WIN;
                    }
                } else {
                    counterP = 0;
                    counterNP = 0;
                }
            }
        }

        // rows
        for (int row = 1; row <= height - k; row++) {
            counterP = 0;
            counterNP = 0;
            for (int tmpRow = row, col = width - 1; col >= 0 && tmpRow < height; tmpRow++, col--) {
                if (grid[linearise(col, tmpRow)] == MAX) {
                    counterP++;
                    counterNP = 0;
                    if (counterP == k) {
                        won = true;
                        ret += WIN;
                    }
                } else if (grid[linearise(col, tmpRow)] == -MAX) {
                    counterNP++;
                    counterP = 0;
                    if (counterNP == k) {
                        won = true;
                        ret -= WIN;
                    }
                } else {
                    counterP = 0;
                    counterNP = 0;
                }
            }
        }
        return ret;
    }

    private int checkNewWin(int move) {
        int ret = 0;
        int row = getRow(move);
        int col = getCol(move);
        int counterP;
        int counterNP;


        // vert
        counterNP = 0;
        counterP = 0;
        for (int check = 0; check < height; check++) {
            if (grid[linearise(col, check)] == MAX) {
                counterP++;
                counterNP = 0;
                if (counterP == k) {
                    ret += WIN;
                    won = true;
                }
            } else if (grid[linearise(col, check)] == -MAX) {
                counterNP++;
                counterP = 0;
                if (counterNP == k) {
                    ret -= WIN;
                    won = true;
                }
            } else {
                counterNP = 0;
                counterP = 0;
            }
        }

        // horiz
        counterNP = 0;
        counterP = 0;
        for (int check = 0; check < width; check++) {
            if (grid[linearise(check, row)] == MAX) {
                counterP++;
                counterNP = 0;
                if (counterP == k) {
                    ret += WIN;
                    won = true;
                }
            } else if (grid[linearise(check, row)] == -MAX) {
                counterNP++;
                counterP = 0;
                if (counterNP == k) {
                    ret -= WIN;
                    won = true;
                }
            } else {
                counterNP = 0;
                counterP = 0;
            }
        }

        // tl - br
        int min = Math.min(row, col);
        counterNP = 0;
        counterP = 0;
        for (int checkRow = row - min, checkCol = col - min; checkRow < height && checkCol < width; checkRow++, checkCol++) {
            if (grid[linearise(checkCol, checkRow)] == MAX) {
                counterP++;
                counterNP = 0;
                if (counterP == k) {
                    ret += WIN;
                    won = true;
                }
            } else if (grid[linearise(checkCol, checkRow)] == -MAX) {
                counterNP++;
                counterP = 0;
                if (counterNP == k) {
                    ret -= WIN;
                    won = true;
                }
            } else {
                counterNP = 0;
                counterP = 0;
            }
        }
        // tr - bl
        int tmpRow = row;
        int tmpCol = col;
        while (tmpCol < width - 1 && tmpRow > 0) {
            tmpCol++;
            tmpRow--;
        }
        counterNP = 0;
        counterP = 0;
        for (int checkRow = tmpRow, checkCol = tmpCol; checkRow < height && checkCol >= 0; checkRow++, checkCol--) {
            if (grid[linearise(checkCol, checkRow)] == MAX) {
                counterP++;
                counterNP = 0;
                if (counterP == k) {
                    ret += WIN;
                    won = true;
                }
            } else if (grid[linearise(checkCol, checkRow)] == -MAX) {
                counterNP++;
                counterP = 0;
                if (counterNP == k) {
                    ret -= WIN;
                    won = true;
                }
            } else {
                counterNP = 0;
                counterP = 0;
            }
        }
        return ret;
    }

    private int initEval() {
        return checkWin() + checkThreats();
    }

    private int evaluate(int oldEval, int move) {
        return oldEval + checkNewWin(move) + checkNewThreats(move);
    }

    private int pieceCounter(int[] grid, int move) {
        int row = getRow(move);
        int col = getCol(move);
        int val = 0;
        int numPiecesP;
        int counterP;
        int numPieces;
        int counter;

        // col
        numPiecesP = 0;
        counterP = 0;
        numPieces = 0;
        counter = 0;
        for (int r = 0; r < height; r++) {
            if (grid[linearise(col, r)] == -MAX) {
                if (counter >= k) val += numPieces;
                numPieces = 0;
                counter = 0;
                numPiecesP++;
                counterP++;
            } else if (grid[linearise(col, r)] == MAX) {
                if (counterP >= k) val -= numPiecesP;
                numPieces++;
                counter++;
                numPiecesP = 0;
                counterP = 0;
            } else {
                counter++;
                counterP++;
            }
        }
        if (counter >= k) val += numPieces;
        if (counterP >= k) val -= numPiecesP;

        // row
        numPiecesP = 0;
        counterP = 0;
        numPieces = 0;
        counter = 0;
        for (int c = 0; c < width; c++) {
            if (grid[linearise(c, row)] == -MAX) {
                if (counter >= k) val += numPieces;
                numPieces = 0;
                counter = 0;
                numPiecesP++;
                counterP++;
            } else if (grid[linearise(c, row)] == MAX) {
                if (counterP >= k) val -= numPiecesP;
                numPieces++;
                counter++;
                numPiecesP = 0;
                counterP = 0;
            } else {
                counter++;
                counterP++;
            }
        }
        if (counter >= k) val += numPieces;
        if (counterP >= k) val -= numPiecesP;

        // tl-br
        int min = Math.min(row, col);
        numPiecesP = 0;
        counterP = 0;
        numPieces = 0;
        counter = 0;
        for (int checkRow = row - min, checkCol = col - min; checkRow < height && checkCol < width; checkRow++, checkCol++) {
            if (grid[linearise(checkCol, checkRow)] == -MAX) {
                if (counter >= k) val += numPieces;
                numPieces = 0;
                counter = 0;
                numPiecesP++;
                counterP++;
            } else if (grid[linearise(checkCol, checkRow)] == MAX) {
                if (counterP >= k) val -= numPiecesP;
                numPieces++;
                counter++;
                numPiecesP = 0;
                counterP = 0;
            } else {
                counter++;
                counterP++;
            }
        }
        if (counter >= k) val += numPieces;
        if (counterP >= k) val -= numPiecesP;

        // tr-bl
        int tmpRow = row;
        int tmpCol = col;
        while (tmpCol < width - 1 && tmpRow > 0) {
            tmpCol++;
            tmpRow--;
        }
        numPiecesP = 0;
        counterP = 0;
        numPieces = 0;
        counter = 0;

        for (int checkRow = tmpRow, checkCol = tmpCol; checkRow < height && checkCol >= 0; checkRow++, checkCol--) {
            if (grid[linearise(checkCol, checkRow)] == -MAX) {
                if (counter >= k) val += numPieces;
                numPieces = 0;
                counter = 0;
                numPiecesP++;
                counterP++;
            } else if (grid[linearise(checkCol, checkRow)] == MAX) {
                if (counterP >= k) val -= numPiecesP;
                numPieces++;
                counter++;
                numPiecesP = 0;
                counterP = 0;
            } else {
                counter++;
                counterP++;
            }
        }
        if (counter >= k) val += numPieces;
        if (counterP >= k) val -= numPiecesP;

        return val;
    }

    public boolean isWon() {
        return won;
    }

    public boolean isLeaf() {
        return getMoves().size() == 0;
    }

    // ************************************************************************
    // static methods
    // ************************************************************************

    /**
     * Read in an MNK domain from a file.
     *
     * @param filename the file to read
     * @return the generated MNK environment
     * @throws FileNotFoundException if the file does not exist
     */
    public static MNK fromFile(String filename) throws FileNotFoundException {
        Scanner in = new Scanner(new File(filename));
        int M = in.nextInt();
        int N = in.nextInt();
        int k = in.nextInt();
        int[][] grid = new int[M][N];
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                grid[m][n] = in.nextInt();
            }
        }
        in.close();
        return new MNK(grid, k, MNK.X);
    }

    /**
     * Write an MNK environment to a file.
     *
     * @param board the board to write to the file
     * @param filename the file to write to
     * @throws IOException if the file could not be written
     */
    public static void toFile(MNK board, String filename) throws IOException {
        FileWriter out = new FileWriter(filename);
        int M = board.getHeight();
        int N = board.getWidth();
        out.write(M + " " + N + " " + board.getK() + "\n");
        for (int y = 0; y < M; y++) {
            for (int x = 0; x < N; x++) {
                out.write(board.getPos(x, y) + " ");
            }
        }
        out.close();
    }

}
