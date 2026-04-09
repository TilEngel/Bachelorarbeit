package main.java.hsg;

import main.java.database.graph.Edge;
import main.java.events.ttps.*;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;

public class MatchingEngine {
    private static List<TTP> initialCompromise1= List.of(new Untrusted_Read());
    private static List<TTP> establishFoothold= List.of(new Shell_Exec());
    private static List<TTP> privilegeEscalation = List.of(new Switch_SU());
    private static List<TTP> cleanupTracks = List.of(new Clear_Logs());
    private static List<List<TTP>> allTTPs = List.of(initialCompromise1, establishFoothold, privilegeEscalation,cleanupTracks);


    public static void matchTTPs(ProvenanceGraph graph){
        int count = 0;
        for(Edge e: graph.getEdges()){
            for(List<TTP> phase : allTTPs){
                for(TTP ttp: phase){
                    if(ttp.matches(e, graph)){
                        count ++;
                        System.out.println("[TTP] Match auf "+ e.getDstNode().getName()+ ": "+ ttp.getName());
                    }
                }
            }
        }
        System.out.println("Count: "+ count);
    }
}
