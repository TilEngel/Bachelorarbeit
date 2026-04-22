package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.database.graph.Node;
import main.java.events.ttps.TTP;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static main.java.Main.ALARM_THRESHOLD;

public class ScoringEngine {

    /**
     * Berechnet zu jedem Szenario die Bedrohungspunktzahl und speichert sie
     * sortiert nach Score in eine Liste
     * @param scenarios Liste an Szenarien
     * @return Liste an Szenarien, die auf ihren Thread-Score abgebildet werden
     */
    public static List<Map.Entry<Double, List<Edge>>> scoreSzenarios(Map<String, List<Edge>> scenarios){
        List<Map.Entry<Double, List<Edge>>> rankedScenarios = new ArrayList<>();

        for (Map.Entry<String, List<Edge> > entry : scenarios.entrySet()) {
            List<Edge> involved = entry.getValue();
            double score= 1.0; // BERECHNUNG

            rankedScenarios.add(Map.entry(score, involved));
            Logger.logResult("[RESULT] Szenario Score: " + score);
            if(score >= ALARM_THRESHOLD){
                Logger.logResult("\n[ALARM] GRENZWERT ÜBERSCHRITTEN!!\n ");
            }
        }

        //Szenarien sortieren

        return  rankedScenarios;
    }


    /**
     * Liefert numerischen Wert für Severities
     * (über so eine Methode, damit Werte zentral änderbar sind)
     * @param ttp entsprechendes TTP
     * @return Severity-Wert
     */
    private int getSeverityValue(TTP ttp){
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
