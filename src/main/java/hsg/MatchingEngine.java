package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.events.ttps.*;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.*;

import static main.java.Main.PF_THRESHOLD;

public class MatchingEngine {


    public static void matchTTPs(ProvenanceGraph graph, List<List<TTP>> phases) {
        //Initial_Compromise finden
        for (Edge e : graph.getEdges()) {
            for (TTP ttp : phases.get(0)) { //initial_compromise1
                if (ttp.matches(e, graph)) {
                    Node match = e.getDstNode();
                    //Initial_Compromise entdeckt -> neue Kette starten
                    TTPChain newChain = new TTPChain(ttp.getName(), match.getHashId());
                    match.addChain(newChain);
                    match.addTTP(ttp);
                    Logger.log("[INFO] New Chain " + ttp.getName() + " auf " + match.getName());

                }
            }
        }

        //Kette verfolgen und auf spätere Phasen testen
        for (Node startNode : graph.getNodes().values()) {
            if (!startNode.getChains().isEmpty()) {
                //Vom Startknoten zu erreichende Knoten durchlaufen
                Queue<String> queue = new LinkedList<>();
                Map<String, Integer> visitedPF = new HashMap<>();
                queue.add(startNode.getHashId());
                visitedPF.put(startNode.getHashId(), 1);

                while (!queue.isEmpty()) {
                    String currentId = queue.poll();
                    Node currentNode = graph.getNode(currentId);
                    int currentPF = visitedPF.get(currentId);

                    for (Edge e : graph.getOutEdges(currentId)) {
                        Node dstNode = e.getDstNode();
                        String dstId = dstNode.getHashId();
                        int newPF = computeNewPF(currentNode,dstNode, currentPF, graph);
                        //Wenn PF>Threshold, wird Kette abgebrochen
                        if(newPF <= PF_THRESHOLD) {

                            //TTP Matching
                            for (List<TTP> phase : phases) {
                                for (TTP ttp : phase) {
                                    if (ttp.matches(e, graph)) {

                                        for (TTPChain chain : currentNode.getChains()) {
                                            if (!chain.getTtps().contains(ttp.getName())) {
                                                //Kette erweitern
                                                TTPChain extend = chain.extendChain(ttp.getName(), newPF);
                                                Logger.log("--[INFO] Chain erweitert" + extend + " auf " + dstNode.getName());
                                                dstNode.addChain(extend);
                                                dstNode.addTTP(ttp);

                                            }
                                        }
                                    }
                                }
                            }
                        }
                        //Auch ohne Match weiter traversieren
                        if(!visitedPF.containsKey(dstId)|| visitedPF.get(dstId) >newPF ){

                            visitedPF.put(dstId,newPF);
                            queue.add(dstId);
                        }

                    }
                }


            }
        }
    }


    private static int computeNewPF(Node srcNode, Node dstNode, int currentPF, ProvenanceGraph graph){
        if(!(dstNode instanceof Subject)){
            return currentPF;
        }
        for(Edge e: graph.getInEdges(dstNode.getHashId())){
            if(e.getOperation().equals(EventType.Type.EVENT_FORK.toString()) && e.getSrcNode().getHashId().equals((srcNode.getHashId()))){
                return currentPF;
            }
        }
        return currentPF +1;
    }
}
