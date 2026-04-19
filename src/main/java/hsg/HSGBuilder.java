package main.java.hsg;

import main.java.database.graph.Node;
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
    public static Map<String, List<Node>> constructHSG(ProvenanceGraph graph){

        Map<String, List<Node>> scenarios = new HashMap<>();

        for(Node node: graph.getNodes().values()){
            //Für jede Kette aller Knoten
            for(TTPChain chain: node.getChains()){
                String origin = chain.getOriginId();
                //Wenn noch kein Szenario mit dem Ursprung existiert
                if(!scenarios.containsKey(origin)){
                    //Neuen Eintrag erstellen
                    scenarios.put(origin, new ArrayList<>());
                }
                //Wenn Knoten noch nicht in der Liste seines Szenarios enthalten ist
                if(!scenarios.get(origin).contains(node)){
                    //Hinzufügen
                    scenarios.get(origin).add(node);
                }
            }
        }

        return scenarios;
    }

    /**
     * Gibt alle Szenarien aus
     * @param scenarios Auszugebene Szenarien
     */
    public static void printScenarios(Map<String,List<Node>> scenarios, ProvenanceGraph graph) {
        int count = 0;
        for (Map.Entry<String, List<Node>> entry : scenarios.entrySet()) {
            count++;
            Node origin = graph.getNode(entry.getKey());
            List<Node> involved = entry.getValue();

            System.out.println("\n Szenario " + count);
            System.out.println("Ursprung: " + origin.getName());
            System.out.println("Beteiligte Knoten: " + involved.size());

            //TTPs
            Set<String> allTTPs = new LinkedHashSet<>();
            for (Node n : involved) {
                for (TTPChain chain : n.getChains()) {
                    if (chain.getOriginId().equals(entry.getKey())) {
                        allTTPs.addAll(chain.getTtps());
                    }
                }
            }
            System.out.println("TTP-Kette: " + allTTPs);
        }
    }
}
