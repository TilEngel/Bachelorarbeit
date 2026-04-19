package main.java.hsg;

import main.java.database.graph.Node;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.*;

public class HSGBuilder {

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

}
