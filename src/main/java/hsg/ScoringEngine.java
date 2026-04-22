package main.java.hsg;

import main.java.database.graph.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ScoringEngine {

    /**
     * Berechnet zu jedem Szenario die Bedrohungspunktzahl und speichert sie
     * sortiert nach Score in eine Liste
     * @param scenarios Liste an Szenarien
     * @return Liste an Szenarien, die auf ihren Thread-Score abgebildet werden
     */
    public static List<Map.Entry<Double, List<Node>>> scoreSzenarios(Map<String, List<Node>> scenarios){
        List<Map.Entry<Double, List<Node>>> rankedSzenarios = new ArrayList<>();

        //Berechnung

        return rankedSzenarios;
    }
}
