package graft.phases;

/**
 * The result of a Graft phase run.
 *
 * @author Wim Keirsgieter
 */
public class PhaseResult {

    private String phaseClass;
    private boolean wasSuccessful;
    private String details;

    /**
     * Instantiate a new phase result.
     *
     * @param phase the phase that was run
     * @param success true if the run was successful, otherwise false
     */
    public PhaseResult(GraftPhase phase, boolean success, String details) {
        phaseClass = phase.getClass().getSimpleName();
        wasSuccessful = success;
        this.details = details;
    }

    /**
     * Returns true if the result indicates the phase run was successful, else false.
     *
     * @return true if the run was successful, else false
     */
    public boolean wasSucessful() {
        return wasSuccessful;
    }

    @Override
    public String toString() {
        String s = "====================================================================================================\n";
        s += "|                                                                                                  |\n";
        s += String.format("| Phase result: %1$-82s |\n", phaseClass);
        s += String.format("| Success: %1$-87b |\n", wasSuccessful);
        s += "| Details:                                                                                         |\n";
        s += "|                                                                                                  |\n";
        s += details;
        s += "|                                                                                                  |\n";
        s += "====================================================================================================\n";

        return s;
    }

}
