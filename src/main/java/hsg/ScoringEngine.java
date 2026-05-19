package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.events.ttps.TTP;

import java.util.*;

import static java.lang.Math.pow;
import static main.java.Main.*;

public class ScoringEngine {

    /**
     * Berechnet zu jedem Szenario die Bedrohungspunktzahl und speichert sie
     * sortiert nach Score in eine Liste
     * @param scenarios Liste an Szenarien
     * @return Liste an Szenarien, die auf ihren Thread-Score abgebildet werden
     */
    public static List<Map.Entry<Double, List<Edge>>> scoreScenarios(Map<String, List<Edge>> scenarios){
        List<Map.Entry<Double, List<Edge>>> rankedScenarios = new ArrayList<>();

        int count =0;
        for (Map.Entry<String, List<Edge> > entry : scenarios.entrySet()) {
            String originId = entry.getKey();
            count++;
            List<Edge> involved = entry.getValue();

            double score= computeScore(involved, originId);
            if(ROUND_THREAT_SCORES) { //Wert auf eine Nachkommastelle runden
                score = Math.round(score * 10.0) / 10.0;
            }

            rankedScenarios.add(Map.entry(score, involved));
            Logger.logResult("[RESULT] Szenario "+ count+ " Score: " + score);
            if(score >= ALARM_THRESHOLD){
                Logger.logResult("\n[ALARM] GRENZWERT ÜBERSCHRITTEN!!\n ");
            }
        }

        //Szenarien nach Score absteigend sortieren
        rankedScenarios.sort((a,b) -> Double.compare(b.getKey(),a.getKey() ));

        return  rankedScenarios;
    }


    /**
     * Berechnet den Score für ein Szenario.
     * Beachtet dabei, den kritischsten TTP-Typ pro Phase zu verwenden
     * @param involved zu bewertendes Szenario
     * @return Bedrohungspunktzahl
     */
    private static double computeScore(List<Edge> involved,String originId){

        List<String> phaseOrder = List.of( //Reihenfolge der Phasen
                "initial_compromise", "establish_foothold",
                "privilege_escalation", "internal_recon","move_laterally",
                "maintain_presence", "cleanup_tracks"
        );

        double score = 1.0;
        Map<String, Integer> ps = findRelevantScores(involved, originId);
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
     * Findet aus einer Liste an Kanten die höchsten Scores
     * der Phasen. Nur ein Score pro Phase
     * @param involved Liste an Kanten in einem Szenario
     * @return Map <Phase -> höchster score>
     */
    private static Map<String ,Integer> findRelevantScores(List<Edge> involved, String originId){
        Map<String, Integer> phases = new HashMap<>();
        for (Edge e : involved){
            Node n = e.getDstNode();
            Set<String> scenarioTTPs = new HashSet<>();
            for(TTPChain chain: n.getChains()){
                //TTPs des Szenarios identifizieren
                if (chain.getOriginId().equals(originId)){
                    String ttp = chain.getTTPForNode(n);
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
     * Gibt die Szenarien sortiert nach Threat-Score mit Threat-Score aus
     * @param rankedScenarios Bewertete Szenarien (durch ScoringEngine.scoreScenarios)
     */
    public static void printRankedScenarios(List<Map.Entry<Double,List<Edge>>> rankedScenarios){
        Logger.logPriority("\n++Szenarien (Sortiert absteigend nach Bedrohlichkeit)++ \n");
        int count = 0;
        for (Map.Entry<Double, List<Edge>> entry : rankedScenarios) {
            count++;
            List<Edge> involved = entry.getValue();
            double score = entry.getKey();
            if (score >= MENTION_SCENARIO_THRESHOLD) {
                String origin = null;
                for (Edge e : involved) {
                    if (!e.getDstNode().getChains().isEmpty()) {
                        origin = e.getDstNode().getChains().get(0).getOriginId();
                        break;
                    }
                }

                Logger.logSemiResult("\n Szenario " + count);
                Logger.logSemiResult("Threat-Score: " + score);
                Logger.logSemiResult("Beteiligte Knoten: " + involved.size());
                if (score >= ALARM_THRESHOLD) {
                    Logger.logSemiResult("\nGEFAHR\n");
                    HSGBuilder.printScenario(involved);
                }


                //TTPs des Szenarios sammeln
                Set<String> allTTPs = new LinkedHashSet<>();
                for (Edge n : involved) {
                    for (TTPChain chain : n.getDstNode().getChains()) {
                        if (chain.getOriginId().equals(origin)) {
                            allTTPs.addAll(chain.getTtps().keySet());
                        }
                    }
                }
                Logger.logSemiResult("TTP-Kette: " + allTTPs);
            }
        }
    }

    /**
     * Liefert numerischen Wert für Severities
     * (über so eine Methode, damit Werte zentral änderbar sind)
     * @param ttp entsprechendes TTP
     * @return Severity-Wert
     */
    private static int getSeverityValue(TTP ttp){
        char severity = ttp.getSeverity();

        if(severity == 'L'){
            return 2;
        }
        if(severity == 'M'){
            return  6;
        }
        if(severity== 'H'){
            return  8;
        }else{
            return 10;
        }
    }
}
