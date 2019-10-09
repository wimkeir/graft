package domains;

import java.util.ArrayList;
import java.util.List;

public class MNK implements GameTree {

    public static int X = 1;
    public static int O = -1;

    private int[] grid;
    private int width, height, k, searchDepth, player;
    private int evaluation;

    public MNK(int m, int n, int k, int searchDepth, int player) {
        this.width = m;
        this.height = n;
        this.k = k;
        this.searchDepth = searchDepth;
        this.player = player;
        evaluation = 0;
    }

    public MNK(int[][] grid, int k, int searchDepth, int player) {
        this.height = grid.length;
        this.width = grid[0].length;
        int c = 0;
        this.grid = new int[height*width];
        for (int[] i : grid) {
            for (int j : i) {
                assert j >= -1 && j <= 1;
                this.grid[c++] = j;
            }
        }
        this.player = player;
        this.searchDepth  =searchDepth;
        this.k = k;
        this.evaluation = initEval();
    }

    private MNK(int[] grid, int m, int k, int searchDepth, int player, int move, int oldEval) {
        this.grid = grid;
        this.width = m;
        this.height = grid.length/m;
        this.k = k;
        this.searchDepth = searchDepth;
        this.player = player;
        this.evaluation = evaluate(oldEval, move);
    }

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

    private int linearize(int col, int row) {
        return width * row + col;
    }

    private int getCol(int r) {
        return r % width;
    }

    private int getRow(int r) {
        return  r / width;
    }

    @Override
    public GameTree makeMove(Integer i) {
        GameTree child;
        int[] childGrid = grid.clone();
        childGrid[i] = player;
        child = new MNK(childGrid, width, k, searchDepth-1, -player, i, evaluation);
        return child;
    }

    @Override
    public int getEvaluation() {
        return evaluation;
    }

    @Override
    public int getPlayer() {
        return player;
    }

    @Override
    public int hashFunction() {
        return grid.hashCode();
    }

    @Override
    public void draw() {
        int c = 0;
        for (int i : grid) {
            if (c++ == width) {
                System.out.println();
                c = 1;
            }
            System.out.print(i + " ");
        }
        System.out.println();
    }

    @Override
    public boolean isTerminal() {
        return searchDepth == 0 || evaluation >= 1000;
    }

    @Override
    public int searchDepth() {
        return searchDepth;
    }

    @Override
    public int treeDepth() {
        return 0;
    }

    public int getPos(int x, int y) {
        int pos = linearize(x, y);
        return grid[pos];
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    private int checkThreats() {
        return 0;
    }

    private int checkWin() {
        return 0;
    }

    private int initEval() {
        /*
        TODO: check wins, check threats, check half threats
         */
        return checkWin() + checkThreats();
    }

    private int evaluate(int oldEval, int move) {
        return oldEval + checkNewWin(move) + checkNewThreats(move);
    }


    private int checkNewThreats(int move) {
        return 0;
    }

    private int checkNewWin(int move) {
        return 0;
    }


    public static void main(String[] args) {
        int [][] g =    {{-1, 1, 0},
                         {0, -1, 1},
                         {1, 0, -1} };
        //int[][] grid, int k, int searchDepth, int player
        MNK mnk = new MNK(g, 3, 4, 1);
        mnk.draw();
        MNK mnk2 = (MNK) mnk.makeMove(8);
        mnk2.draw();
        System.out.println(mnk.getPos(2,2));
    }
}
