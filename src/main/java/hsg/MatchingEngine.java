package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.events.ttps.*;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;

public class MatchingEngine {
    private static List<TTP> initialCompromise1= List.of(new Untrusted_Read());
    private static List<TTP> initialCompromise2= List.of(new Untrusted_File_Exec(), new Make_Mem_Exec());
    private static List<TTP> establishFoothold= List.of(new Shell_Exec(), new CnC());
    private static List<TTP> privilegeEscalation = List.of(new Switch_SU());
    private static List<TTP> internalRecon = List.of(new Sensitive_Command());
    private static List<TTP> cleanupTracks = List.of(new Clear_Logs(), new Sensitive_Temp_RM());
    private static List<List<TTP>> allTTPs = List.of(initialCompromise1, initialCompromise2,
            establishFoothold, privilegeEscalation,internalRecon,cleanupTracks);


    public static void matchTTPs(ProvenanceGraph graph){
        int count = 0;
        for(Edge e: graph.getEdges()){
            for(List<TTP> phase : allTTPs){
                for(TTP ttp: phase){
                    if(ttp.matches(e, graph)){
                        count ++;
                        Logger.logTTPMatch(e.getDstNode(), ttp);
                    }
                }
            }
        }
        Logger.log("Count: "+ count);
    }
}
