package graft.phases;

/**
 * This interface defines a phase of a Graft run.
 *
 * Classes that implement this interface can be registered on a Graft run and will be run in
 * sequence, with their results consolidated in the run report.
 *
 * @author Wim Keirsgieter
 */
public interface GraftPhase {

    /**
     * Run the given phase and return the result.
     *
     * @return the result of the phase run
     */
    PhaseResult run();

}
