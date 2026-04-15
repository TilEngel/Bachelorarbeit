package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.events.ttps.*;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;

public class MatchingEngine {
    //TODO Listen in main auslagern und als Parameter übergeben



    public static void matchTTPs(ProvenanceGraph graph, List<List<TTP>> phases){
        int count = 0;
        for(Edge e: graph.getEdges()){
            for(List<TTP> phase : phases){
                for(TTP ttp: phase){
                    if(ttp.matches(e, graph)){
                        count ++;
                        Logger.logTTPMatch(e.getDstNode(), ttp);
                    }
                }
            }
        }
        Logger.logResult("Count: "+ count);
    }
}
