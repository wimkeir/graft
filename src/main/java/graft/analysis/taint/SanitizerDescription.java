package graft.analysis.taint;

import org.apache.tinkerpop.gremlin.structure.Vertex;

public abstract class SanitizerDescription {

    public abstract boolean sanitizes(Vertex cfgNode, String varName);

}
