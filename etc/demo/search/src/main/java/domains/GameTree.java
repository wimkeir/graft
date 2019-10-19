package domains;

import java.util.List;

/**
 * An interface for two-player adversarial game tree domains.
 */
public interface GameTree {

    /**
     * Get a list of legal moves from this node.
     *
     * @return a list of legal moves
     */
    List<Integer> getMoves();

    /**
     * Apply the given move to the node and return the corresponding child.
     *
     * @param i the move to apply
     * @return the resulting child
     */
    GameTree makeMove(Integer i);

    /**
     * Get the evaluation of the current node.
     *
     * @return the evaluation of the node
     */
    int getEvaluation();

    /**
     * Get the evaluation of the node, taking into account search depth.
     *
     * @param depth the current search depth
     * @return the evaluation of the node
     */
    int getEvaluation(int depth);

    /**
     * Get the perspective of the domain.
     *
     * @return the current player perspective
     */
    int getPlayer();

    /**
     * Get the hash value of the node.
     *
     * @return the hash of the node
     */
    int hashFunction();

    /**
     * Display the game tree in the terminal.
     */
    void draw();

    /**
     * Check whether the node is a terminal node in the tree.
     *
     * @return true if the node is terminal, else false
     */
    boolean isTerminal();

    long getHash();
}

