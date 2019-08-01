package graft.analysis.taint;

import java.util.ArrayList;
import java.util.List;

/**
 * A sanitizer description that describes methods which sanitizes their arguments.
 *
 * If no arguments are specified as being sanitized, then all arguments are assumed sanitized.
 *
 * @author Wim Keirsgieter
 */
public class MethodSanitizer extends SanitizerDescription {

    public String namePattern;
    public String scopePattern;

    public List<Integer> sanitizesArgs;

    /**
     * Returns a new sanitizer description with patterns describing the name and scope of the methods,
     * and a list of args sanitized by the matching methods.
     *
     * @param namePattern a regex describing the pattern of the method name
     * @param scopePattern a regex describing the pattern of the method scope
     * @param sanitizesArgs a list of arguments considered sanitized by the method (empty list implies all)
     */
    public MethodSanitizer(String namePattern, String scopePattern, int... sanitizesArgs) {
        // TODO: match on method signature
        this.namePattern = namePattern;
        this.scopePattern = scopePattern;

        this.sanitizesArgs = new ArrayList<>();
        for (int arg : sanitizesArgs) {
            this.sanitizesArgs.add(arg);
        }
    }

}
