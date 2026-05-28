package main.java.events;

import main.java.database.graph.Edge;

public interface Prerequisite {
    boolean evaluate(Edge edge);
}
