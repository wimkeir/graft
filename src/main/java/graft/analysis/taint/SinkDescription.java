package graft.analysis.taint;

import java.util.ArrayList;
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

    public String namePattern;
    public String scopePattern;
    public List<Integer> sinksArgs;

    /**
     * Returns a new sink description with patterns describing the name and scope of the methods, and a list of
     * args sunk by the matching methods.
     *
     * @param namePattern a regex describing the pattern of the method name
     * @param scopePattern a regex describing the pattern of the method scope
     * @param sinksArgs a list of arguments considered sunk by the method (empty list implies all)
     */
    public SinkDescription(String namePattern, String scopePattern, int... sinksArgs) {
        // TODO: match on method signature
        this.namePattern = namePattern;
        this.scopePattern = scopePattern;

        this.sinksArgs = new ArrayList<>();
        for (int arg : sinksArgs) {
            this.sinksArgs.add(arg);
        }
    }

}
