package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.events.ttps.*;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.*;

import static main.java.Main.*;

/**
 * Soll Einhaltung der TTPs prüfen und entsprechende Schritte einleiten
 * Soll TTP.matches(edge) aufrufen, PathFactor berechnen und Szenarien erstellen
 */
public class HSGBuilder {


    /**
     * Sucht nach Initial_Compromise.
     * Verfolgt Kette an zusammenhängenden Ereignissen (unter Berücksichtigung PF).
     * Hält aufeinanderfolgende TTPs in TTPChains fest
     * @return Szenarien mit Startknoten
     */
    public static List<Scenario> matchTTPs() {

        Map<String, Scenario> scenarios = new HashMap<>();
        Set<String> startNodes = new HashSet<>();
        Map<String, Long> earliestVisit = new HashMap<>();

        //Initial_Compromise finden
        for (Edge e : ProvenanceGraph.getEdges()) {
            for (TTP ttp : PHASES.get(0)) { //initial_compromise1
                if (ttp.matches(e)) {
                    Node match = e.getDstNode();
                    //Könnte schon durch andere Kante Instanz haben
                    if(match.getChains().isEmpty()) {
                        //Initial_Compromise entdeckt -> neue Kette starten
                        TTPChain newChain = new TTPChain(ttp.getName(), e, e.getTimestampRec());
                        match.addChain(newChain);
                        match.addTTP(ttp);
                        startNodes.add(match.getHashId());

                        Scenario scenario = new Scenario(match.getHashId());
                        scenario.addTTPEdge(e,1);
                        scenarios.put(match.getHashId(), scenario);
                        if(!earliestVisit.containsKey(match.getHashId()) || earliestVisit.get(match.getHashId())> Long.parseLong(e.getTimestampRec())){
                            earliestVisit.put(match.getHashId(), Long.parseLong(e.getTimestampRec()));
                        }

                        Logger.log("[INFO] New Chain " + ttp.getName() + " auf " + match.getName());

                    }
                }
            }
        }

        //Kette verfolgen und auf spätere Phasen testen

        for (String startId : startNodes) {
            Node startNode = ProvenanceGraph.getNode(startId);
            if (!startNode.getChains().isEmpty()) {
                //Vom Startknoten zu erreichende Knoten durchlaufen
                Queue<String> queue = new LinkedList<>();
                Map<String, Integer> visitedPF = new HashMap<>();
                queue.add(startNode.getHashId());
                visitedPF.put(startNode.getHashId(), 1); //PF zu Beginn 1

                //Breitensuche
                while (!queue.isEmpty()) {
                    String currentId = queue.poll();
                    Node currentNode = ProvenanceGraph.getNode(currentId);
                    int currentPF = visitedPF.get(currentId);


                    for (Edge e : ProvenanceGraph.getOutEdges(currentId)) {
                        //wenn zeitlich schlüssig
                        if (earliestVisit.get(currentId) < Long.parseLong(e.getTimestampRec())) {

                            Node dstNode = e.getDstNode();
                            String dstId = dstNode.getHashId();
                            //prüfen, ob durch FORK entstanden
                            if(e.getOperation().equals(EventType.Type.EVENT_FORK.toString())){
                                for(TTPChain chain: currentNode.getChains()){
                                    if(chain.isForkDescendant(currentId)){
                                        chain.addFork(dstId);
                                    }
                                }
                            }
                            //Neuen PF bestimmen
                            int newPF = computeNewPF(dstNode, currentPF, currentNode.getChains());

                            //Wenn PF>Threshold, wird Kette abgebrochen
                            if (newPF <= PF_THRESHOLD) {
                                List<TTPChain> changedChains = new ArrayList<>();

                                //TTP Matching
                                matchOnEdge(currentNode, e, newPF, scenarios, changedChains);

                                //Ketten an Nachfolger weitergeben, wenn durch Matching noch nicht geschehen
                                List<TTPChain> copyNew = new ArrayList<>(currentNode.getChains());
                                for (TTPChain chain : copyNew) {

                                    if (!changedChains.contains(chain)) {
                                        //PF anpassen, wenn nötig
                                        TTPChain ex = (newPF == chain.getPathFactor()) ? chain : chain.updatePF(newPF);

                                        if(!dstNode.hasChain(ex)){
                                            dstNode.addChain(ex);
                                        }

                                    }
                                }


                                //Auch ohne Match zum nächsten Knoten traversieren
                                //Knoten werden erneut traversiert, wenn ein kürzerer Pfad gefunden wurde
                                if (!visitedPF.containsKey(dstId) || visitedPF.get(dstId) > newPF) {

                                    visitedPF.put(dstId, newPF);

                                    if(!earliestVisit.containsKey(dstId) ||Long.parseLong(e.getTimestampRec())< earliestVisit.get(dstId)){
                                        earliestVisit.put(dstId, Long.parseLong(e.getTimestampRec()));
                                    }
                                    queue.add(dstId);
                                }

                            }

                        }
                    }


                }
            }
        }

        return new ArrayList<>(scenarios.values());
    }

    /**
     * Matcht auf TTPs an aktueller Kante.
     * Fügt gefundenes TTP in TTPChain und Szenario ein
     * @param currentNode Aktueller Knoten
     * @param e Kante an dem Knoten
     * @param newPF PathFactor
     * @param scenarios Alle Szenarien
     * @param changedChains Liste an Chains, die sich verändert haben
     */
    private static void matchOnEdge(Node currentNode, Edge e, int newPF, Map<String,Scenario> scenarios, List<TTPChain> changedChains){
        Node dstNode = e.getDstNode();
        //Kopie, über die iteriert wird, weil dem Knoten in der Schleife Chains hinzugefügt werden können (exception)
        List<TTPChain> copy = new ArrayList<>(currentNode.getChains());

        for (TTPChain chain : copy) {
            for (List<TTP> phase : PHASES) {
                for (TTP ttp : phase) {
                    if (!chain.getTtps().containsKey(ttp.getName())) {
                        if (ttp.matches(e, chain.getOriginId())) {
                            //Zeitliche Reihenfolge beachten
                            if(Long.parseLong(e.getTimestampRec()) > Long.parseLong(chain.getOriginTimestamp())){
                                if (!e.getSrcNode().getName().equals(e.getDstNode().getName())) {
                                    //Kette erweitern
                                    TTPChain extend = chain.extendChain(ttp.getName(), newPF,e);
                                    //Nur wenn (inhaltlich) gleiche Chain noch nicht existiert
                                    if (!dstNode.hasChain(extend)) {
                                        Logger.log("[INFO] Chain erweitert auf " + dstNode.getName() + " (PF= "+extend.getPathFactor()+ ")");

                                        dstNode.addChain(extend);
                                        changedChains.add(chain); //Damit nicht weitergegeben
                                        dstNode.addTTP(ttp);

                                        scenarios.get(extend.getOriginId()).addTTPEdge(e, newPF);

                                    }
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
     * @param dstNode Zielknoten
     * @param currentPF aktueller PF
     * @return currentPF++, wenn nötig. Sonst currentPF
     */
    private static int computeNewPF(Node dstNode, int currentPF, List<TTPChain> chains){
        if(!(dstNode instanceof Subject)){
            return currentPF;
        }
        //Wenn dstNode durch FORK entstanden ist
        for(TTPChain chain: chains){
            if(chain.isForkDescendant(dstNode.getHashId())){
                return currentPF;
            }
        }
        return currentPF +1;
    }

}
