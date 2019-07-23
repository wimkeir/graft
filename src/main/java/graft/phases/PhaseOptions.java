package graft.phases;

import java.util.HashMap;
import java.util.Map;

/**
 * A container class for options for a Graft phase.
 *
 * @author Wim Keirsgieter
 */
public class PhaseOptions {

    private Map<String, String> options;

    public PhaseOptions() {
        options = new HashMap<>();
    }

    public String getOption(String key) {
        return options.get(key);
    }

    public void setOption(String key, String value) {
        options.put(key, value);
    }

    public Iterable<String> keys() {
        return options.keySet();
    }

}
