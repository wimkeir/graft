package graft.utils;

/**
 * Miscellaneous utility methods for display and output.
 *
 * @author Wim Keirsgieter
 */
public class DisplayUtil {

    /**
     * Get the given elapsed time in human-readable form.
     *
     * @param millis elapsed time in milliseconds
     * @return the elapsed time in human-readable form.
     */
    public static String displayTime(long millis) {
        if (millis < 1000) {
            return displayMillis(millis);
        } else if (millis < 60000) {
            return displaySeconds(millis);
        } else {
            return displayMinutes(millis);
        }
    }

    private static String displayMillis(long millis) {
        return millis + "ms";
    }

    private static String displaySeconds(long millis) {
        return (millis / 1000) + " seconds (" + millis + "ms)";
    }

    private static String displayMinutes(long millis) {
        return (millis / 60000) + " minutes (" + millis + "ms)";
    }

}
