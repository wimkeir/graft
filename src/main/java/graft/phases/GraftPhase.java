package graft.phases;

/**
 * This interface defines a phase of a Graft run.
 *
 * Classes that implement this interface can be registered on a Graft run and will be run in
 * sequence.
 *
 * @author Wim Keirsgieter
 */
public interface GraftPhase {

    /**
     * Run the given phase.
     */
    void run();

}
