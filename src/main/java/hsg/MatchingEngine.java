package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.events.ttps.*;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.*;

import static main.java.Main.OPTIMIZED_RESULTS;
import static main.java.Main.PF_THRESHOLD;

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
                visitedPF.put(startNode.getHashId(), 1); //PF zu Beginn 1

                while (!queue.isEmpty()) {
                    String currentId = queue.poll();
                    Node currentNode = graph.getNode(currentId);
                    int currentPF = visitedPF.get(currentId);

                    for (Edge e : graph.getOutEdges(currentId)) {
                        Node dstNode = e.getDstNode();
                        String dstId = dstNode.getHashId();
                        //Neuen PF bestimmen
                        int newPF = computeNewPF(currentNode,dstNode, currentPF, graph);
                        //Wenn PF>Threshold, wird Kette abgebrochen
                        if(newPF <= PF_THRESHOLD) {

                            //TTP Matching
                            for (List<TTP> phase : phases) {
                                for (TTP ttp : phase) {
                                    if (ttp.matches(e, graph)) {

                                        //Kopie, über die iteriert wird, weil dem Knoten in der Schleife Chains hinzugefügt werden können (exception)
                                        List<TTPChain> copy = new ArrayList<>(currentNode.getChains());
                                        for (TTPChain chain : copy) {
                                            if (!chain.getTtps().contains(ttp.getName())) {
                                                //Kette erweitern
                                                TTPChain extend = chain.extendChain(ttp.getName(), newPF);
                                                //Nur wenn (inhaltlich) gleiche Chain noch nicht existiert
                                                if (!dstNode.hasChain(extend)) {
                                                    Logger.log("--[INFO] Chain erweitert" + extend + " auf " + dstNode.getName());
                                                    dstNode.addChain(extend);
                                                    dstNode.addTTP(ttp);
                                                }

                                            }
                                        }
                                    }
                                }
                            }

                            if (OPTIMIZED_RESULTS) {
                                //Auch ohne Match zum nächsten Knoten traversieren
                                //Knoten werden erneut traversiert, wenn ein kürzerer Pfad gefunden wurde
                                if (!visitedPF.containsKey(dstId) || visitedPF.get(dstId) > newPF) {

                                    visitedPF.put(dstId, newPF);
                                    queue.add(dstId);
                                }
                            }
                        }

                    }
                }

            }
        }
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
