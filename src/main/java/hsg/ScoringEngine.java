package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.events.ttps.TTP;

import java.util.*;

import static java.lang.Math.pow;
import static main.java.Main.ALARM_THRESHOLD;
import static main.java.Main.ROUND_THREAT_SCORES;

public class ScoringEngine {

    /**
     * Berechnet zu jedem Szenario die Bedrohungspunktzahl und speichert sie
     * sortiert nach Score in eine Liste
     * @param scenarios Liste an Szenarien
     * @return Liste an Szenarien, die auf ihren Thread-Score abgebildet werden
     */
    public static List<Map.Entry<Double, List<Edge>>> scoreSzenarios(Map<String, List<Edge>> scenarios){
        List<Map.Entry<Double, List<Edge>>> rankedScenarios = new ArrayList<>();

        int count =0;
        for (Map.Entry<String, List<Edge> > entry : scenarios.entrySet()) {

            count++;
            List<Edge> involved = entry.getValue();

            double score= computeScore(involved);
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
    private static double computeScore(List<Edge> involved){
        double score = 1.0;
        Map<String, Integer> phases = new HashMap<>(); //Kill-Chain-Phase -> höchster Score
        for(int i= 0; i< involved.size(); i++){
            //Gewichtung aus dem Paper
            double weight = (10.0 + i+1) / 10.0;

            Node node = involved.get(i).getDstNode();
            //Wert für diesen Knoten bestimmen
            int highest = 1;
            for(TTP ttp : node.getTTPs()){
                int temp = getSeverityValue(ttp);
                // Nur das kritischste TTP pro Phase
                if(!phases.containsKey(ttp.getPhase()) || phases.get(ttp.getPhase())< temp) {
                    if (temp > highest) {
                        highest = temp;
                        phases.put(ttp.getPhase(), temp);
                    }
                }
            }
            double severity= highest;

            score *= pow(severity, weight);
        }
        return  score;
    }


    /**
     * Gibt die Szenarien sortiert nach Threat-Score mit Threat-Score aus
     * @param rankedScenarios Bewertete Szenarien (durch ScoringEngine.scoreScenarios)
     */
    public static void printRankedScenarios(List<Map.Entry<Double,List<Edge>>> rankedScenarios){
        System.out.println("\n++Szenarien (Sortiert absteigend nach Bedrohlichkeit)++ \n");
        int count = 0;
        for (Map.Entry<Double, List<Edge>> entry : rankedScenarios) {
            count++;
            List<Edge> involved = entry.getValue();
            double score = entry.getKey();
            String origin = involved.get(0).getDstNode().getChains().get(0).getOriginId();

            System.out.println("\n Szenario " + count);
            System.out.println("Threat-Score: " + score);
            if(score >= ALARM_THRESHOLD){
                System.out.println("\nGEFAHR\n");
            }
            System.out.println("Beteiligte Knoten: " + involved.size());

            //TTPs des Szenarios sammeln
            Set<String> allTTPs = new LinkedHashSet<>();
            for (Edge n : involved) {
                for (TTPChain chain : n.getDstNode().getChains()) {
                    if (chain.getOriginId().equals(origin)) {
                        allTTPs.addAll(chain.getTtps());
                    }
                }
            }
            System.out.println("TTP-Kette: " + allTTPs);
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
