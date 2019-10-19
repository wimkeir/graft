package domains;

import utils.PiIterator;

import java.io.IOException;
import java.util.*;

/**
 * An implementation of the digits of pi game tree domain from Knuth and Moore.
 */
public class DigitsOfPi implements GameTree {

    // for visualisation purposes
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_RESET = "\u001B[0m";

    private int evaluationVal;
    private boolean isTerminal;
    private int depth;
    private int treeDepth;
    private boolean marked;

    // children in game tree
    private DigitsOfPi left;
    private DigitsOfPi mid;
    private DigitsOfPi right;

    // ************************************************************************
    // constructors
    // ************************************************************************

    /**
     * Initialise a new digits of pi domain down to the given tree depth.
     *
     * @param depth the depth of the tree
     */
    public DigitsOfPi(int depth) {

        this.treeDepth = 0;
        PiIterator it = null;
        try {
            it = new PiIterator();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.depth = depth;
        if (depth == 0) {
            this.isTerminal = true;
            this.evaluationVal = it.next();
        } else {
            this.left = new DigitsOfPi(depth - 1, it, 1);
            this.mid = new DigitsOfPi(depth - 1, it, 1);
            this.right = new DigitsOfPi(depth - 1, it, 1);
        }
    }

    private DigitsOfPi(int depth, PiIterator it, int treeDepth) {
        this.depth = depth;
        this.treeDepth = treeDepth;
        if (depth == 0) {
            this.isTerminal = true;
            this.evaluationVal = it.next();
        } else {
            int tmp = depth - 1;
            int tmpd = treeDepth + 1;
            this.left = new DigitsOfPi(tmp, it, tmpd);
            this.mid = new DigitsOfPi(tmp, it, tmpd);
            this.right = new DigitsOfPi(tmp, it, tmpd);
        }
    }

    // ************************************************************************
    // implemented GameTree methods
    // ************************************************************************

    @Override
    public List<Integer> getMoves() {
        List<Integer> l = new ArrayList<>();
        if (!isTerminal) {
            l.add(0);
            l.add(1);
            l.add(2);
        }

        return l;
    }

    @Override
    public GameTree makeMove(Integer i) {
        if (i.equals(0)) {
            return this.left;
        } else if (i.equals(1)) {
            return this.mid;
        } else if (i.equals(2)) {
            return this.right;
        } else {
            return null;
        }
    }

    @Override
    public int getEvaluation() {
        marked = true;
        return evaluationVal;
    }

    @Override
    public int getEvaluation(int depth) {
        marked = true;
        return evaluationVal;
    }

    @Override
    public int getPlayer() {
        return 0;
    }

    @Override
    public int hashFunction() {
        if (this.depth == 0) {
            return evaluationVal;
        }
        return left.hashFunction() + mid.hashFunction() + right.hashFunction();
    }

    @Override
    public void draw() {
        Queue<DigitsOfPi> q = new ArrayDeque<>();
        q.add(this);
        int counter = 0;
        while (!q.isEmpty()) {
            Queue<DigitsOfPi> tmp = new ArrayDeque<>();
            System.out.print((counter++) + ":    ");
            while (!q.isEmpty()) {
                DigitsOfPi next = q.poll();
                if (next.isMarked()) {
                    System.out.print(ANSI_BLUE + next.getEvaluation() + ANSI_RESET + " ");
                } else {
                    System.out.print(ANSI_RED + next.getEvaluation() + ANSI_RESET + " ");
                }
                if (next.left != null) {
                    tmp.add(next.left);
                }
                if (next.mid != null) {
                    tmp.add(next.mid);
                }
                if (next.right != null) {
                    tmp.add(next.right);
                }
            }
            System.out.println();
            q = tmp;
        }
    }

    @Override
    public boolean isTerminal() {
        return isTerminal;
    }

    @Override
    public long getHash() {
        return 0;
    }

    // ************************************************************************
    // other public methods
    // ************************************************************************

    /**
     * Back-propagates pruning information from leaves and then draws the board.
     */
    public void show() {
        backProp();
        this.draw();
    }

    // ************************************************************************
    // private methods
    // ************************************************************************

    private void backProp() {
        if (!isTerminal) {
            left.backProp();
            mid.backProp();
            right.backProp();
            this.marked = left.isMarked() || mid.isMarked() || right.isMarked();
        }
    }

    private boolean isMarked () {
        return marked;
    }

}
