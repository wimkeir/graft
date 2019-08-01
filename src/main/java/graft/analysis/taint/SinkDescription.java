package graft.analysis.taint;

import java.util.List;

/**
 * A description of a sensitive sink (specifically a method call).
 *
 * An argument is considered "sunk" if it is specified as such in the description. If no arguments are specified,
 * then all args are considered sunk.
 *
 * @author Wim Keirsgieter
 */
public class SinkDescription {

    public String sigPattern;
    public List<Integer> sinksArgs;

    /**
     * Returns a new sink description with patterns describing the name and scope of the methods, and a list of
     * args sunk by the matching methods.
     *
     * @param sigPattern a regex describing the pattern of the method name
     * @param sinksArgs a list of arguments considered sunk by the method (empty list implies all)
     */
    public SinkDescription(String sigPattern, List<Integer> sinksArgs) {
        this.sigPattern = sigPattern;
        this.sinksArgs = sinksArgs;
    }

    @Override
    public String toString() {
        return sigPattern;
    }

}
