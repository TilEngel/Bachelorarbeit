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
    public static List<Scenario> matchTTPs(ProvenanceGraph graph) {

        Map<String, Scenario> scenarios = new HashMap<>();
        Set<String> startNodes = new HashSet<>();

        //Initial_Compromise finden
        for (Edge e : graph.getEdges()) {
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
                        scenario.addTTPEdge(e);
                        scenarios.put(match.getHashId(), scenario);

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

                //Breitensuche
                while (!queue.isEmpty()) {
                    String currentId = queue.poll();
                    Node currentNode = graph.getNode(currentId);
                    int currentPF = visitedPF.get(currentId);

                    for (Edge e : graph.getOutEdges(currentId)) {
                        Node dstNode = e.getDstNode();
                        String dstId = dstNode.getHashId();
                        //if(startNodes.contains(dstId)) continue;
                        //Neuen PF bestimmen
                        int newPF = computeNewPF(currentNode, dstNode, currentPF, graph);

                        //Wenn PF>Threshold, wird Kette abgebrochen
                        if (newPF <= PF_THRESHOLD) {
                            List<TTPChain> changedChains = new ArrayList<>();

                            //TTP Matching
                            matchOnEdge(currentNode,e,newPF,scenarios,changedChains);

                            //Ketten an Nachfolger weitergeben, wenn durch Matching noch nicht geschehen
                            List<TTPChain> copyNew = new ArrayList<>(currentNode.getChains());
                            for (TTPChain chain : copyNew) {
                                //PF anpassen
                                if (!changedChains.contains(chain)) {
                                    TTPChain ex;
                                    if(newPF == chain.getPathFactor()){
                                        ex = chain;
                                    } else {
                                        ex = chain.updatePF(newPF);
                                    }

                                    //Verbindungskante (gestrichelte Linien im Graphen)
                                    String oid = chain.getOriginId();
                                    Scenario scen = scenarios.get(oid);
                                    if(scen != null && !scen.hasEdge(e)
                                            &&!e.getSrcNode().getName().equals(e.getDstNode().getName())
                                            &&Long.parseLong(e.getTimestampRec()) >= Long.parseLong(chain.getOriginTimestamp())){

                                        scen.addConnectingEdge(e);

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
        for(Scenario scenario: scenarios.values()){
            Comparator<Edge> comp = Comparator.comparingLong(e->Long.parseLong(e.getTimestampRec()));
            scenario.getTTPEdges().sort(comp);
            scenario.getConnectingEdges().sort(comp);

        }

        List<Scenario> scenarioList = new ArrayList<>(scenarios.values());
        if(REMOVE_DUPLICATE_SCENARIOS){
            //Duplikate entfernen, Szenarien mit identischer Kantenabfolge
            //Node.hasChain prüft gleichheit auf Knotenebene, trotzdem kommt es zu duplikaten
            //(vermutlich durch Versionierung der Knoten)#
            removeDuplicates(scenarioList);
        }

        return scenarioList;
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
                                        Logger.log("[INFO] Chain erweitert" + extend + " auf " + dstNode.getName());

                                        dstNode.addChain(extend);
                                        changedChains.add(chain); //Damit nicht weitergegeben
                                        dstNode.addTTP(ttp);

                                        scenarios.get(extend.getOriginId()).addTTPEdge(e);

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



    /**
     * Schaut, ob zwei Szenarien die gleiche Länge haben und die Kanten aus den gleichen Knoten bestehen
     * @param s1 erstes Szenario
     * @param s2 zweites Szenario
     * @return true, wenn Anzahl und Namen der Knoten identisch sind
     */
    private static boolean isSameScenario(Scenario s1, Scenario s2){
        List<Edge> edges1 = s1.getTTPEdges();
        List<Edge> edges2 = s2.getTTPEdges();
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
     * Entfernt doppelte Szenarien aus Gruppe
     * @param scenarios Gruppe an Szenarien
     */
    private static void removeDuplicates(List<Scenario> scenarios){
        Set<Integer> remove = new HashSet<>();
        //Jedes Szenario mit jedem anderen vergleichen
        for (int i = 0; i < scenarios.size(); i++) {
            if (!remove.contains(i)) {

                for (int j = i + 1; j < scenarios.size(); j++) {
                    if (!remove.contains(j)) {

                        //Falls Szenarien inhaltlich identisch sind
                        if (isSameScenario(scenarios.get(i), scenarios.get(j))) {
                            remove.add(j);
                        }
                    }
                }
            }
        }
        List<Integer> sorted = new ArrayList<>(remove);
        sorted.sort(Comparator.reverseOrder());
        for (int id : remove) {
            scenarios.remove(id);
        }

    }



}
