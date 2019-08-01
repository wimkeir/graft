package graft.analysis.taint;

import graft.analysis.AnalysisResult;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import static graft.Const.*;

public class TaintAnalysisResult extends AnalysisResult {

    private Vertex source;
    private Vertex sink;
    private String varName;
    private String sourcePattern;
    private String sinkPattern;

    public TaintAnalysisResult(Vertex source, Vertex sink, String varName, String sourcePattern, String sinkPattern) {
        this.source = source;
        this.sink = sink;
        this.varName = varName;
        this.sourcePattern = sourcePattern;
        this.sinkPattern = sinkPattern;
    }

    @Override
    public String toString() {
        String s = "| ************************************* Taint vulnerability! ************************************* |\n";
        s += "|                                                                                                  |\n";
        s += String.format("| Source: %1$-88s |\n", source.value(TEXT_LABEL).toString());
        s += String.format("| Matches: %1$-87s |\n", sourcePattern);
        s += String.format("| Location: %1$-86s |\n", String.format("%s (line %s)", source.value(METHOD_SIG).toString(), source.value(LINE_NO).toString()));
        s += "|                                                                                                  |\n";
        s += String.format("| Sink: %1$-90s |\n", sink.value(TEXT_LABEL).toString());
        s += String.format("| Matches: %1$-87s |\n", sinkPattern);
        s += String.format("| Location: %1$-86s |\n", String.format("%s (line %s)", sink.value(METHOD_SIG).toString(), sink.value(LINE_NO).toString()));
        return s;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TaintAnalysisResult)) {
            return false;
        }

        TaintAnalysisResult that = (TaintAnalysisResult) obj;
        return this.source.equals(that.source) &&
                this.sink.equals(that.sink) &&
                this.varName.equals(that.varName) &&
                this.sourcePattern.equals(that.sourcePattern) &&
                this.sinkPattern.equals(that.sinkPattern);
    }
}
