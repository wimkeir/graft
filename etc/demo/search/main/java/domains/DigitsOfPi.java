package domains;

import utils.PiGenerator;

import java.util.*;

public class DigitsOfPi implements GameTree {

    private int evaluationVal;
    private boolean isTerminal;
    private int depth;
    private int treeDepth;
    private DigitsOfPi left;
    private DigitsOfPi mid;
    private DigitsOfPi right;

    public DigitsOfPi(int depth) {

        this.treeDepth = 0;
        PiGenerator it = new PiGenerator();
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

    private DigitsOfPi(int depth, PiGenerator it, int treeDepth) {
        this.depth = depth;
        this.treeDepth = treeDepth;
        if (depth == 0) {
            this.isTerminal = true;
            this.evaluationVal = it.next();
        } else {
            this.left = new DigitsOfPi(depth - 1, it, treeDepth + 1);
            this.mid = new DigitsOfPi(depth - 1, it, treeDepth + 1);
            this.right = new DigitsOfPi(depth - 1, it, treeDepth + 1);
        }
    }

    public List<GameTree> getChildren() {
        List<GameTree> l = new ArrayList<>(2);
        l.add(this.left);
        l.add(this.mid);
        l.add(this.right);
        return l;
    }

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
                System.out.print(next.getEvaluation() + " ");
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
    public int searchDepth() {
        return depth;
    }

    @Override
    public int treeDepth() {
        return treeDepth;
    }

    public static void main(String[] args) {
        DigitsOfPi p = new DigitsOfPi(4);
        p.draw();
    }
}
