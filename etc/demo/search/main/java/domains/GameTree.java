package domains;

import java.util.List;

public interface GameTree {
    List<Integer> getMoves();
    GameTree makeMove(Integer i);
    int getEvaluation();
    int getPlayer();
    int hashFunction();
    String toString();
    void draw();
    boolean isTerminal();
    int searchDepth();
    int treeDepth();
}

