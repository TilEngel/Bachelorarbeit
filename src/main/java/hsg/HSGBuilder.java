package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.events.ttps.TTP;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.*;

/**
 * Erstellt Listen an Knoten, welche als HSG verstanden
 * werden können.
 */
public class HSGBuilder {

    /**
     * Sammelt alle Knoten, deren TTPChain auf den gleichen Ursprung
     * verweisen in einer Liste, die ein Angriffsszenario repräsentiert
     * @param graph Provenance-Graph
     * @return Map, die Ursprungs-Knoten auf Liste abbildet
     */
    public static Map<String, List<Edge>> constructHSG(ProvenanceGraph graph){

        Map<String, List<Edge>> scenarios = new HashMap<>();

        for(Edge edge: graph.getEdges()){
            //Für jede Kette aller Knoten
            for(TTPChain chain: edge.getDstNode().getChains()){
                String origin = chain.getOriginId();
                //Wenn noch kein Szenario mit dem Ursprung existiert
                if(!scenarios.containsKey(origin)){
                    //Neuen Eintrag erstellen
                    scenarios.put(origin, new ArrayList<>());
                }
                //Wenn Knoten noch nicht in der Liste seines Szenarios enthalten ist
                if(!scenarios.get(origin).contains(edge)){
                    scenarios.get(origin).add(edge);

                }
            }
        }
        //Nach dem Einfügen Kanten sortieren (aufsteigend nach TTPChain-Länge).
        //Damit Kanten in Reihenfolge, wie sie aufgetreten sind
        for(List<Edge> edges : scenarios.values()){
            edges.sort((edge1, edge2) ->{
                int length1 = getMinChainLength(edge1.getDstNode());
                int length2 = getMinChainLength(edge2.getDstNode());
                return Integer.compare(length1, length2);
            });
        }

        return scenarios;
    }

    /**
     * Gibt alle Szenarien aus
     * @param scenarios auszugebene Szenarien
     */
    public static void printScenarios(Map<String,List<Edge>> scenarios, ProvenanceGraph graph) {
        int count = 0;
        for (Map.Entry<String, List<Edge>> entry : scenarios.entrySet()) {
            count++;
            Node origin = graph.getNode(entry.getKey());
            List<Edge> involved = entry.getValue();

            Logger.logResult("\n Szenario " + count);
            Logger.logResult("Ursprung: " + origin.getName());
            Logger.logResult("Beteiligte Knoten: " + involved.size());

            //TTPs
            TTPChain longest= null;
            for(Edge e: involved){
                for(TTPChain chain : e.getDstNode().getChains()){
                    if(longest == null || chain.getTtps().size()> longest.getTtps().size()){
                        longest= chain;
                    }
                }
            }
            if(longest!= null){
                Logger.logResult("TTP-Kette: "+ longest.getTtps());
            }

        }
    }


    private static int getMinChainLength(Node node){
        int min = Integer.MAX_VALUE;
        for(TTPChain chain: node.getChains()){
            if(chain.getTtps().size()<min){
                min = chain.getTtps().size();
            }
        }
        return min;
    }
}
