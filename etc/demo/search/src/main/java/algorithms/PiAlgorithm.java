package algorithms;

/**
 * An interface for algorithms that can evaluate and visualise the digits of pi domain.
 */
public interface PiAlgorithm {

    /**
     * Evaluate and visualise the digits of pi game tree down to a given depth.
     *
     * @param depth the depth to which the game tree should be evaluated
     */
    void evaluate(int depth);
}
