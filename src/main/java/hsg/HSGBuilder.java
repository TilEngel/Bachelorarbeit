package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.*;

import static main.java.Main.REMOVE_DUPLICATE_SCENARIOS;


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
    public static Map<String, List<Edge>> constructHSG(ProvenanceGraph graph) {

        Map<String, List<Edge>> scenarios = new HashMap<>();

        for (Edge edge : graph.getEdges()) {
            //Für jede Kette aller Knoten
            for (TTPChain chain : edge.getDstNode().getChains()) {
                String origin = chain.getOriginId();
                //Wenn noch kein Szenario mit dem Ursprung existiert
                if (!scenarios.containsKey(origin)) {
                    //Neuen Eintrag erstellen
                    scenarios.put(origin, new ArrayList<>());
                }
                //Wenn Knoten noch nicht in der Liste seines Szenarios enthalten ist
                if (!scenarios.get(origin).contains(edge)) {
                    if (!alreadyHasEdge(scenarios.get(origin), edge)) {
                        scenarios.get(origin).add(edge);
                    }

                }
            }
        }
        //Nach dem Einfügen Kanten sortieren (aufsteigend nach Timestamp).
        //Damit Kanten in Reihenfolge, wie sie aufgetreten sind
        for(List<Edge> edges : scenarios.values()){
            edges.sort((edge1, edge2)->Long.compare(Long.parseLong(edge1.getTimestampRec()), Long.parseLong(edge2.getTimestampRec())));
        }

        if(REMOVE_DUPLICATE_SCENARIOS){
            //Duplikate entfernen, Szenarien mit identischer Kantenabfolge
            //Node.hasChain prüft gleichheit auf Knotenebene, trotzdem kommt es zu duplikaten
            //(vermutlich durch Versionierung der Knoten)#
            List<String> originIds = new ArrayList<>(scenarios.keySet());
            Set<String> remove = new HashSet<>();
            //Jedes Szenario mit jedem anderen vergleichen
            for (int i = 0; i < originIds.size(); i++) {
                if (!remove.contains(originIds.get(i))) {
                    List<Edge> edges1 = scenarios.get(originIds.get(i));

                    for (int j = i + 1; j < originIds.size(); j++) {
                        if (!remove.contains(originIds.get(j))) {
                            List<Edge> edges2 = scenarios.get(originIds.get(j));
                            //Falls Szenarien inhaltlich identisch sind
                            if (isSameScenario(edges1, edges2)) {
                                remove.add(originIds.get(j));
                            }
                        }
                    }
                }
            }
            for (String id : remove) {
                scenarios.remove(id);
            }
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

            Logger.logSemiResult("\n Szenario " + count);
            Logger.logSemiResult("Ursprung: " + origin.getName());
            Logger.logSemiResult("Beteiligte Kanten: " + involved.size());

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
                Logger.logSemiResult("TTP-Kette: "+ longest.getTtps());
            }

            //HSG-Knoten ausgeben
            Map<String, List<Edge>> ttpEdges = new LinkedHashMap<>();

            for(Edge e : involved){
                for(TTPChain chain: e.getDstNode().getChains()){
                    if(chain.getOriginId().equals(entry.getKey())){
                        String lastTTP = chain.getLastTTP();
                        if(!ttpEdges.containsKey(lastTTP)){
                            ttpEdges.put(lastTTP,new ArrayList<>());
                        }
                        if(!ttpEdges.get(lastTTP).contains(e)){
                            if(!e.getDstNode().getTTPs().isEmpty()) {
                                ttpEdges.get(lastTTP).add(e);
                            }
                        }
                    }
                }

            }
            StringBuilder sb = new StringBuilder();
            boolean first = true;
            for(Map.Entry<String, List<Edge>> ttpEntry : ttpEdges.entrySet()){
                String ttpName = ttpEntry.getKey();
                List<Edge> ttpMatches = ttpEntry.getValue();

                for(Edge e : ttpMatches){
                    if(!first){
                        sb.append("\n             |\n            \\/\n");
                    }
                    sb.append("["+ e.getSrcNode().getName()+ "]---")
                            .append(ttpName)
                            .append("---["+ e.getDstNode().getName()+ "]");
                    first = false;
                }
            }
            Logger.logSemiResult(sb.toString());
        }
    }



    /**
     * Prüft, ob ein Knoten bereits durch eine andere eingehende Kante vertreten ist,
     * um mehrfach-Matches zu vermeiden
     * @param scenario aktuelles Szenario
     * @param edge aktuelle eingehende Kante
     * @return true, wenn Kante auf einen Knoten führt, der bereits durch eine andere Kante vertreten ist
     */
    private static boolean alreadyHasEdge(List<Edge> scenario,Edge edge){
        for(Edge e: scenario){
            Node n = e.getDstNode();
            if(n.equals(edge.getDstNode())){
                return true;
            }
        }
        return false;
    }

    /**
     * Schaut, ob zwei Szenarien die gleiche Länge haben und die Kanten aus den gleichen Knoten bestehen
     * @param edges1 erstes Szenario
     * @param edges2 zweites Szenario
     * @return true, wenn Anzahl und Namen der Knoten identisch sind
     */
    private static boolean isSameScenario(List<Edge> edges1, List<Edge> edges2){
        if(edges1.size() != edges2.size()){
            return false;
        }
        Set<String> nodeNames1 = new HashSet<>();
        Set<String> nodeNames2 = new HashSet<>();

        for(Edge e: edges1){
            nodeNames1.add(e.getSrcNode().getName()+e.getDstNode().getName());

        }
        for (Edge e: edges2){
            nodeNames2.add(e.getSrcNode().getName()+e.getDstNode().getName());
        }
        return nodeNames1.equals(nodeNames2);
    }
}
