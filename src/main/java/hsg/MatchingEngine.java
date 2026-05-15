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
import static main.java.Main.REMOVE_DUPLICATE_SCENARIOS;

/**
 * Soll Einhaltung der TTPs prüfen und entsprechende Schritte einleiten
 * Soll TTP.matches(edge) aufrufen und ggf. HSG-Knoten-Erstellung anfordern
 */
public class MatchingEngine {


    /**
     * Sucht nach Initial_Compromise.
     * Verfolgt Kette an zusammenhängenden Ereignissen (unter Berücksichtigung PF).
     * Hält aufeinanderfolgende TTPs in TTPChains fest
     * @param phases Zu suchende TTPs jeweils in Listen nach Phase
     */
    public static Map<String,List<Edge>> matchTTPs(ProvenanceGraph graph, List<List<TTP>> phases) {

        Map<String, List<Edge>> scenarios = new HashMap<>();

        Set<String> startNodes = new HashSet<>();
        //Initial_Compromise finden
        for (Edge e : graph.getEdges()) {
            for (TTP ttp : phases.get(0)) { //initial_compromise1
                if (ttp.matches(e)) {
                    Node match = e.getDstNode();
                    //Könnte schon durch andere Kante Instanz haben
                    if(match.getChains().isEmpty()) {
                        //Initial_Compromise entdeckt -> neue Kette starten
                        TTPChain newChain = new TTPChain(ttp.getName(), match, e.getTimestampRec());
                        match.addChain(newChain);
                        match.addTTP(ttp);
                        startNodes.add(match.getHashId());
                        scenarios.put(match.getHashId(), new ArrayList<>());
                        scenarios.get(match.getHashId()).add(e);
                        Logger.log("[INFO] New Chain " + ttp.getName() + " auf " + match.getName());

                    }
                }
            }
        }

        //Kette verfolgen und auf spätere Phasen testen

            for (String startId : startNodes) {
                Node startNode = graph.getNode(startId);
                if (!startNode.getChains().isEmpty()) {
                    //Vom Startknoten zu erreichende Knoten durchlaufen
                    Queue<String> queue = new LinkedList<>();
                    Map<String, Integer> visitedPF = new HashMap<>();
                    queue.add(startNode.getHashId());
                    visitedPF.put(startNode.getHashId(), 1); //PF zu Beginn 1

                    while (!queue.isEmpty()) {
                        String currentId = queue.poll();
                        Node currentNode = graph.getNode(currentId);
                        int currentPF = visitedPF.get(currentId);

                        for (Edge e : graph.getOutEdges(currentId)) {
                            Node dstNode = e.getDstNode();
                            String dstId = dstNode.getHashId();
                            if(startNodes.contains(dstId)) continue;
                            //Neuen PF bestimmen
                            int newPF = computeNewPF(currentNode, dstNode, currentPF, graph);
                            //Wenn PF>Threshold, wird Kette abgebrochen
                            if (newPF <= PF_THRESHOLD) {
                                List<TTPChain> changedChains = new ArrayList<>();
                                //TTP Matching
                                //Kopie, über die iteriert wird, weil dem Knoten in der Schleife Chains hinzugefügt werden können (exception)
                                List<TTPChain> copy = new ArrayList<>(currentNode.getChains());
                                for (TTPChain chain : copy) {

                                    for (List<TTP> phase : phases) {
                                        for (TTP ttp : phase) {
                                            if (!chain.getTtps().containsKey(ttp.getName())) {
                                                if (ttp.matches(e)) {
                                                    if(Long.parseLong(e.getTimestampRec()) > Long.parseLong(chain.getOriginTimestamp())){
                                                        if (!e.getSrcNode().getName().equals(e.getDstNode().getName())) {// Zuletzt
                                                            //Kette erweitern
                                                            TTPChain extend = chain.extendChain(ttp.getName(), newPF, dstNode);
                                                            //Nur wenn (inhaltlich) gleiche Chain noch nicht existiert
                                                            if (!dstNode.hasChain(extend)) {
                                                                Logger.log("----[INFO] Chain erweitert" + extend + " auf " + dstNode.getName());
                                                                dstNode.addChain(extend);
                                                                changedChains.add(chain); //Damit nicht weitergegeben
                                                                dstNode.addTTP(ttp);

                                                                scenarios.get(extend.getOriginId()).add(e);

                                                            }
                                                        }
                                                    }


                                                }
                                            }

                                        }

                                    }
                                }
                                List<TTPChain> copyNew = new ArrayList<>(currentNode.getChains());
                                for (TTPChain chain : copyNew) {
                                    if (!changedChains.contains(chain)) {
                                        TTPChain ex = chain.updatePF(newPF);
                                            //Verbindungskante
                                            String oid = chain.getOriginId();
                                            if(scenarios.containsKey(oid) && !scenarios.get(oid).contains(e)
                                                    &&!e.getSrcNode().getName().equals(e.getDstNode().getName())
                                                    &&Long.parseLong(e.getTimestampRec()) >= Long.parseLong(chain.getOriginTimestamp())){

                                                scenarios.get(oid).add(e);

                                            }
                                        if (!dstNode.hasChain(ex)) {
                                            dstNode.addChain(ex);
                                        }
                                    }
                                }


                                //Auch ohne Match zum nächsten Knoten traversieren
                                //Knoten werden erneut traversiert, wenn ein kürzerer Pfad gefunden wurde
                                if (!visitedPF.containsKey(dstId) || visitedPF.get(dstId) > newPF ) {

                                    visitedPF.put(dstId, newPF);
                                    queue.add(dstId);
                                }

                            }

                        }


                }
            }
        }

        //Nach dem Einfügen Kanten sortieren (aufsteigend nach Timestamp).
        //Damit Kanten in Reihenfolge, wie sie aufgetreten sind
        for(List<Edge> edges : scenarios.values()){

            edges.sort((edge1, edge2) -> Long.compare(Long.parseLong(edge1.getTimestampRec()), Long.parseLong(edge2.getTimestampRec())));

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


    /**
     * Berechnung einens neuen PF
     * @param srcNode Ursprungsknoten
     * @param dstNode Zielknoten
     * @param currentPF aktueller PF
     * @return currentPF++, wenn nötig. Sonst currentPF
     */
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
