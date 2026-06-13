package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.events.ttps.TTP;

import java.util.*;
import static java.lang.Math.pow;
import static main.java.Main.*;

/**
 * Klasse zur Bewertung der erstellten Szenarien
 */
public class ScoringEngine {

    /**
     * Berechnet den Score für ein Szenario.
     * Beachtet dabei, den kritischsten TTP-Typ pro Phase zu verwenden
     * @param phaseScores Map Phase->Score
     * @return Bedrohungspunktzahl
     */
    private static double computeScore(Map<String, Integer> phaseScores){

        List<String> phaseOrder = List.of( //Reihenfolge der Phasen
                "initial_compromise", "establish_foothold",
                "privilege_escalation", "internal_recon","move_laterally",
                "complete_mission", "cleanup_tracks"
        );

        double score = 1.0;
        int i = 0;
        for(String phase : phaseOrder){
            if(phaseScores.containsKey(phase)) {
                i++;
                //Gewichtung wie im Paper
                double weight = (10 + i) / 10.0;

                score *= pow(phaseScores.get(phase), weight);
            }
        }
        return  score;
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





    /**
     * Berechnet zu einem Szenario die Bedrohungspunktzahl.
     * Stößt eventuell HSGConverter an
     * @param scenario Szenario
     * @param ttp das neue TTP
     * @param origin Ursprungsknoten des Szenarios
     */
    public static void scoreScenarioStreaming(Scenario scenario, TTP ttp, String origin){


        int severity = getSeverityValue(ttp);
        //Wenn kein neuer Score entsteht stoppen
        if(!scenario.updatePhaseScore(ttp.getPhase(),severity)){
            return;
        }

        double score = computeScore(scenario.getRelevantScores());
        if(ROUND_THREAT_SCORES) { //Wert auf eine Nachkommastelle runden
            score = Math.round(score * 10.0) / 10.0;
        }
        if(score > MENTION_SCENARIO_THRESHOLD) {
            Logger.logResult("[RESULT] Score: " + score);
            if (score >= ALARM_THRESHOLD) {
                Logger.logResult("\n[ALARM] GRENZWERT ÜBERSCHRITTEN!!\n ");
                HSGConverter.exportToDOTStreaming(scenario, score, origin);
            }
        }
    }


}
