package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.events.ttps.TTP;
import main.java.events.ttps.TTPConfig;

import java.util.*;
import static java.lang.Math.pow;
import static main.java.Main.*;

/**
 * Klasse zur Bewertung der erstellten Szenarien
 */
public class ScoringEngine {

    /**
     * Berechnet zu jedem Szenario die Bedrohungspunktzahl und speichert sie
     * sortiert nach Score in eine Liste
     * @param scenarios Liste an Szenarien
     * @return Liste an Szenarien, die auf ihren Thread-Score abgebildet werden
     */
    public static List<Scenario> scoreScenarios(List<Scenario> scenarios){
        List<Scenario> rankedScenarios = new ArrayList<>();
        int count =0;
        for (Scenario scenario : scenarios) {
            count++;

            double score= computeScore(scenario);
            if(ROUND_THREAT_SCORES) { //Wert auf eine Nachkommastelle runden
                score = Math.round(score * 10.0) / 10.0;
            }
            if(score > MENTION_SCENARIO_THRESHOLD) {
                scenario.setScore(score);
                rankedScenarios.add(scenario);
                Logger.logResult("[RESULT] Szenario " + count + " Score: " + score);
                if (score >= ALARM_THRESHOLD) {
                    Logger.logResult("\n[ALARM] GRENZWERT ÜBERSCHRITTEN!!\n ");
                }
            }
        }

        //Szenarien nach Score absteigend sortieren
        rankedScenarios.sort(Comparator.comparingDouble(Scenario::getScore).reversed() );
        return  rankedScenarios;
    }


    /**
     * Berechnet den Score für ein Szenario.
     * Beachtet dabei, den kritischsten TTP-Typ pro Phase zu verwenden
     * @param scenario zu bewertendes Szenario
     * @return Bedrohungspunktzahl
     */
    private static double computeScore(Scenario scenario){

        List<String> phaseOrder = List.of( //Reihenfolge der Phasen
                "initial_compromise", "establish_foothold",
                "privilege_escalation", "internal_recon","move_laterally",
                "complete_mission", "cleanup_tracks"
        );

        double score = 1.0;
        Map<String, Integer> ps = findRelevantScores(scenario);
        int i = 0;
        for(String phase : phaseOrder){
            if(ps.containsKey(phase)) {
                i++;
                //Gewichtung wie im Paper
                double weight = (10 + i) / 10.0;

                score *= pow(ps.get(phase), weight);
            }
        }
        return  score;
    }

    /**
     * Findet aus einem Szenario die höchsten Scores
     * der Phasen. Nur ein Score pro Phase
     * @param scenario entsprechendes Szenario
     * @return Map <Phase -> höchster score>
     */
    private static Map<String ,Integer> findRelevantScores(Scenario scenario){
        Map<String, Integer> phases = new HashMap<>();
        String originId = scenario.getOriginId();
        for (Edge e : scenario.getTTPEdges()){
            Node n = e.getDstNode();
            Set<String> scenarioTTPs = new HashSet<>();
            for(TTPChain chain: n.getChains()){
                //TTPs des Szenarios identifizieren
                if (chain.getOriginId().equals(originId)){
                    String ttp = chain.getTTPForEdge(e);
                    if(ttp!=null){
                        scenarioTTPs.add(ttp);
                    }
                }
            }
            for(TTP ttp : n.getTTPs()){
                //Nur TTP des Knotens hinzufügen, das wirklich zur Chain gehört
                if(scenarioTTPs.contains(ttp.getName())){
                    int severity = getSeverityValue(ttp);
                    if(!phases.containsKey(ttp.getPhase()) || phases.get(ttp.getPhase())< severity){
                        phases.put(ttp.getPhase(), severity);
                    }
                }
            }
        }
        return phases;
    }


    /**
     * Liefert numerischen Wert für Severities
     * (über so eine Methode, damit Werte zentral änderbar sind)
     * @param ttp entsprechendes TTP
     * @return Severity-Wert
     */
    private static int getSeverityValue(TTP ttp){
       return TTPConfig.getIntProperty("severity."+ttp.getSeverity(), 1);
    }
}
