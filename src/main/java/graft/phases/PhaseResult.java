package graft.phases;

/**
 * The result of a Graft phase run.
 *
 * @author Wim Keirsgieter
 */
public class PhaseResult {

    private boolean wasSuccessful;

    /**
     * Instantiate a new phase result.
     *
     * @param phase the phase that was run
     * @param success true if the run was successful, otherwise false
     */
    public PhaseResult(GraftPhase phase, boolean success) {
        wasSuccessful = success;
    }

    /**
     * Returns true if the result indicates the phase run was successful, else false.
     *
     * @return true if the run was successful, else false
     */
    public boolean wasSucessful() {
        return wasSuccessful;
    }

}
