package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.events.ttps.*;

import java.util.*;

import static main.java.Main.PF_THRESHOLD;

/**
 * Soll Einhaltung der TTPs prüfen und entsprechende Schritte einleiten
 * Soll TTP.matches(edge) aufrufen, PathFactor berechnen und Szenarien erstellen
 */
public class HSGBuilder {

    private static final Map<String, Scenario> scenarios = new HashMap<>();

    /**
     * Sucht nach Initial_Compromise.
     * Verfolgt Kette an zusammenhängenden Ereignissen (unter Berücksichtigung PF).
     * Hält aufeinanderfolgende TTPs in TTPChains fest
     * @param edge neue Kante
     * @param phases Zu prüfende TTPs in Phasen
     */
    public static void matchTTPsStreaming(Edge edge, List<List<TTP>> phases){
        if(!edge.getSrcNode().getHashId().equals(edge.getDstNode().getHashId())) {
            if (edge.getSrcNode().getChains().isEmpty()) {
                for (TTP ttp : phases.get(0)) { //initial_compromise1 O(1)
                    if (ttp.matches(edge)) {
                        Node match = edge.getDstNode();
                        //Initial_Compromise entdeckt -> neue Kette starten
                        TTPChain newChain = new TTPChain(ttp.getName(), edge, edge.getTimestampRec());


                        match.addChain(newChain);
                        match.addTTP(ttp);

                        scenarios.put(newChain.getOriginId(), new Scenario(edge));

                        edge.getSrcNode().addTTP(ttp);

                        Logger.log("[INFO] New Chain " + ttp.getName() + " auf " + match.getName());

                    }
                }
            } else {
                List<TTPChain> copy = new ArrayList<>(edge.getSrcNode().getChains());
                for (TTPChain chain : copy) { //O(C) = O(1)
                    int currentPF = chain.getPathFactor();
                    int newPF = computeNewPFStreaming(edge, currentPF);

                    if (newPF <= PF_THRESHOLD) {
                        boolean chainAdded = false;
                        for (List<TTP> phase : phases) { //O(1)
                            for (TTP ttp : phase) { //O(1)
                                if (ttp.matches(edge, chain.getOriginId())) { // O(1)
                                    if (!chain.getTtps().containsKey(ttp.getName())) {
                                        //Kette erweitern
                                        TTPChain extend = chain.extendChain(ttp.getName(), newPF, edge);
                                        //Nur wenn (inhaltlich) gleiche Chain noch nicht existiert
                                        if (!edge.getDstNode().hasChain(extend)) {
                                            chainAdded = true;
                                            edge.getDstNode().addChain(extend);
                                            edge.getDstNode().addTTP(ttp);
                                            ScoringEngine.scoreScenarioStreaming(scenarios.get(extend.getOriginId()), edge, ttp, extend.getOriginId());
                                        }
                                    }
                                }
                            }
                        }
                        if (!chainAdded) {
                            if (!edge.getDstNode().hasChain(chain)) {
                                edge.getDstNode().addChain(chain.updatePF(newPF));
                                Scenario scenario = scenarios.get(chain.getOriginId());

                                scenario.addEdge(edge);

                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Berechnung einens neuen PF
     * @param edge neue Kante
     * @param currentPF aktueller PF
     * @return currentPF++, wenn nötig. Sonst currentPF
     */
    private static int computeNewPFStreaming(Edge edge, int currentPF){
        if(!(edge.getDstNode() instanceof Subject)){
            return currentPF;
        }
        if(edge.getOperation().equals(EventType.Type.EVENT_FORK.toString())){
            return currentPF;

        }
        return currentPF +1;
    }
}
