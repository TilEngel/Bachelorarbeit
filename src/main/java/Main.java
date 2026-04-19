package main.java;

import main.java.database.JDBCEngine;
import main.java.events.ttps.*;
import main.java.hsg.HSGBuilder;
import main.java.hsg.MatchingEngine;
import main.java.provenanceGraph.DataCollector;
import main.java.provenanceGraph.ProvenanceGraph;

import java.sql.SQLException;
import java.util.List;

public class Main {
    public static final boolean OPTIMIZED_RESULTS = false;
    public static final int PF_THRESHOLD = 3;

    //Zu suchende TTP-Typen
    private static final List<TTP> initialCompromise1= List.of(new Untrusted_Read());
    private static final List<TTP> initialCompromise2= List.of(new Untrusted_File_Exec(), new Make_Mem_Exec());
    private static final List<TTP> establishFoothold= List.of(new Shell_Exec(), new CnC());
    private static final List<TTP> privilegeEscalation = List.of(new Switch_SU());
    private static final List<TTP> internalRecon = List.of(new Sensitive_Command());
    private static final List<TTP> cleanupTracks = List.of(new Clear_Logs(), new Sensitive_Temp_RM());
    private static final List<List<TTP>> phases = List.of(initialCompromise1, initialCompromise2,
            establishFoothold, privilegeEscalation,internalRecon,cleanupTracks);

    private static ProvenanceGraph graph;
    public static void main(String[] args){
        Logger.doLogAll();
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
        MatchingEngine.matchTTPs(graph, phases);
        HSGBuilder.printScenarios(HSGBuilder.constructHSG(graph), graph);
    }


}
