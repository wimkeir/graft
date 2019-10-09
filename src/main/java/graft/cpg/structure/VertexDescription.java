package graft.cpg.structure;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class VertexDescription {

    public final String NAME;
    public final String LABEL;

    private Map<String, String> propPatterns;
    private VertexDescription childDescr;
    private VertexDescription parentDescr;

    public VertexDescription(String name, String label) {
        this.NAME = name;
        this.LABEL = label;
        this.propPatterns = new HashMap<>();
    }

    public void setChildDescr(VertexDescription childDescr) {
        this.childDescr = childDescr;
    }

    public void setParentDescr(VertexDescription parentDescr) {
        this.parentDescr = parentDescr;
    }

    public VertexDescription getChildDescr() {
        return childDescr;
    }

    public VertexDescription getParentDescr() {
        return parentDescr;
    }

    public Iterable<String> keys() {
        return propPatterns.keySet();
    }

    public String getPropPattern(String key) {
        return propPatterns.get(key);
    }

    public void setPropPattern(String key, String value) {
        propPatterns.put(key, value);
    }

}
