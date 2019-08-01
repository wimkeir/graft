package graft.analysis.taint;

import java.util.List;

/**
 * A sanitizer description that describes methods which sanitizes their arguments.
 *
 * If no arguments are specified as being sanitized, then all arguments are assumed sanitized.
 *
 * @author Wim Keirsgieter
 */
public class MethodSanitizer extends SanitizerDescription {

    public String sigPattern;
    public List<Integer> sanitizesArgs;

    /**
     * Returns a new sanitizer description with patterns describing the signature of the methods,
     * and a list of args sanitized by the matching methods.
     *
     * @param sigPattern a regex describing the signature of the method name
     * @param sanitizesArgs a list of arguments considered sanitized by the method (empty list implies all)
     */
    public MethodSanitizer(String sigPattern, List<Integer> sanitizesArgs) {
        this.sigPattern = sigPattern;
        this.sanitizesArgs = sanitizesArgs;
    }

    @Override
    public String toString() {
        return sigPattern;
    }

}
