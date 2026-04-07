package main.java.events;

import main.java.database.graph.Edge;
import main.java.provenanceGraph.ProvenanceGraph;

public interface Prerequisite {
    boolean evaluate(Edge edge, ProvenanceGraph graph);
}
