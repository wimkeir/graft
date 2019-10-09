package graft.cpg.structure;

// TODO: extend Graph, DirectedGraph<Vertex>?

import graft.traversal.CpgTraversalSource;

/**
 * TODO: detailed javadoc
 */
public interface GGraph {

    // TODO: javadoc interface methods

    CpgTraversalSource traversal();
    CodePropertyGraph asCpg();
    void toDot(String filename);
    void dump(String filename);
    void commit();
}
