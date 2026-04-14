package main.java;

import main.java.database.JDBCEngine;
import main.java.database.graph.Edge;
import main.java.events.ttps.*;
import main.java.hsg.MatchingEngine;
import main.java.provenanceGraph.DataCollector;
import main.java.provenanceGraph.ProvenanceGraph;

import java.sql.SQLException;
import java.util.List;

public class Main {

    private static ProvenanceGraph graph;
    public static void main(String[] args){
        Logger.doLogPriority();
        JDBCEngine jdbc = new JDBCEngine();
        DataCollector collector = new DataCollector(jdbc);
        try {
            jdbc.connect();

            collector.collectData();
            graph = collector.getGraph();

            jdbc.disconnect();

        }catch (SQLException e){
            System.err.println("[ERR] Fehler in Main:" +e.getMessage());
        }
        //collector.printEdges();
        MatchingEngine.matchTTPs(graph);
    }


}
