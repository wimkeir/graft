package graft.cpg.structure;

import java.util.Map;

/**
 *
 */
public class VertexDescription {

    public final String NAME;
    public final String LABEL;
    private Map<String, String> propPatterns;

    public VertexDescription(String name, String label, Map<String, String> propPatterns) {
        this.NAME = name;
        this.LABEL = label;

        // TODO: copy propPatterns instead
        this.propPatterns = propPatterns;
    }

    public Iterable<String> keys() {
        return propPatterns.keySet();
    }

    public String getPropPattern(String key) {
        return propPatterns.get(key);
    }

    public String getPropPattern(String key, String defaultPattern) {
        return propPatterns.getOrDefault(key, defaultPattern);
    }

}
