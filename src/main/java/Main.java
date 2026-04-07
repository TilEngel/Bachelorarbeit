package main.java;

import main.java.database.JDBCEngine;
import main.java.database.graph.Edge;
import main.java.events.ttps.*;
import main.java.provenanceGraph.DataCollector;
import main.java.provenanceGraph.ProvenanceGraph;

import java.sql.SQLException;
import java.util.List;

public class Main {

    private static ProvenanceGraph graph;
    public static void main(String[] args){
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
        findTTPs();
    }

    public static void findTTPs(){
        List<TTP> ttps = List.of(
                new Untrusted_Read(),
                new Switch_SU(),
                new Shell_Exec(),
                new Clear_Logs()
        );
        for (Edge e : graph.getEdges()){
            for(TTP ttp : ttps){
                if(ttp.matches(e, graph)){
                    System.out.println("[TTP] Match auf "+ e.getDstNode().getName()+ ": "+ ttp.getName());
                }
            }
        }
    }
}
