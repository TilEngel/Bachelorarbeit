package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.FileWriter;
import static main.java.Main.MENTION_SCENARIO_THRESHOLD;
import static main.java.Main.SHOW_TIMESTAMPS;

/**
 * Erstellt aus den im HSGBuilder erstellten Szenarien Graphen im DOT-Format
 */
public class HSGConverter {


    /**
     * Erstellt aus einer Liste an Szenarien einen Graphen pro Szenario.
     * Graph enthält Name, Threat-Score und Graphen mit relevanten Knoten
     * @param scenarios Bewertete Szenarien
     */
    public static void exportToDOT(List<Scenario> scenarios){
        File outDir = new File("hsg_output");
        if(!outDir.exists()){
            outDir.mkdir();
        }
        int count =0;
        for(Scenario scenario : scenarios) {
            if (scenario.getScore() > MENTION_SCENARIO_THRESHOLD) {
                count++;
                StringBuilder dot = new StringBuilder();
                //Header
                dot.append("digraph Szenario").append(count).append("{\n");
                dot.append("    rankdir=LR;\n");
                dot.append("    node [shape=box];\n");
                //Titel mit Score
                dot.append("    label=\"Szenario ").append(count)
                        .append(" | Bedrohungspunkzahl: ").append(scenario.getScore()).append("\";\n");
                dot.append("    labelloc=\"t\";\n");
                dot.append("\n    //LN war hier\n\n");
                dot.append("    fontsize=16;\n");
                //Graph erstellen
                drawGraph(scenario, dot, count);
            }
        }
    }

    /*
    Zeichnet den tatsächlichen Graphen
    mit TTP-Kanten und Verbindungskanten
     */
    private static void drawGraph(Scenario scenario, StringBuilder dot, int count){
        Set<String> usedEdges = new HashSet<>();

        for (Edge e :scenario.getTTPEdges()) {
            Set<String> ttpNames = getTTPNames(e, scenario.getOriginId());

            if (!ttpNames.isEmpty()) {
                //Src --ttp--> Dst
                String src = e.getSrcNode().getName();
                String dst = e.getDstNode().getName();
                for (String ttpName : ttpNames) {
                    String pf = " (PF = "+ scenario.getPathFactor(e)+ ")";
                    if(SHOW_TIMESTAMPS) {
                        //Zeit bestimmen
                        //Vorlage von Claude.ai
                        Instant time = Instant.ofEpochSecond(Long.parseLong(e.getTimestampRec()) / 1_000_000_000L);
                        pf += " |" + DateTimeFormatter.ofPattern("HH:mm")
                                .withZone(ZoneId.of("America/New_York"))
                                .format(time) + "|";

                    }
                    String edgeName = src + "->" + dst + ttpName;
                    if (!usedEdges.contains(edgeName)) {
                        usedEdges.add(edgeName);
                        //untrusted_read hervorheben
                        //Vorlage von Claude.ai
                        String attrs = ttpName.equals("untrusted_read")
                                ? "[label=\"" + ttpName + pf+  "\", color=blue, penwidth=2.5, fontcolor=blue, fontsize=13]"
                                : "[label=\"" + ttpName +pf+  "\"]";
                        //Kante einzeichnen
                        dot.append("    \"").append(src).append("\"")
                                .append(" -> \"").append(dst).append("\"")
                                .append(" ").append(attrs).append(";\n");
                    }
                }
            }
        }
        //Kürzeste Wege zwischen TTP-Kanten finden
        for(Edge e:findMinimalPathEdges(scenario)){
                //Graph ist nicht versioniert, darum durch Name unterscheiden anstatt HashID
                String src= e.getSrcNode().getName();
                String dst= e.getDstNode().getName();
                String edgeKey = src+"->"+dst;

                boolean addedEdge = !usedEdges.contains(edgeKey);
                //Wege zeichnen
                if(addedEdge){
                    usedEdges.add(edgeKey);
                    dot.append("    \"").append(src).append("\"")
                            .append("->\"").append(dst).append("\"")
                            .append(" [style=dashed, color=gray];\n");
                }

        }
        dot.append("}\n");
        //in Datei schreiben
        writeToFile(dot, count);
    }

    /*Schreibt StringBuilder in eine Datei
     */
    private static void writeToFile(StringBuilder dot, int count){
        try (FileWriter fw = new FileWriter("hsg_output/szenario"+count+".dot")){
            fw.write(dot.toString());
            Runtime.getRuntime().exec("dot -Tpng hsg_output/szenario"+ count+".dot -o hsg_output/szenario"+count+".png");
        }catch (IOException e){
            Logger.logError("DOT-Export Fehler: "+e.getMessage());
        }
    }

    /*
    Findet für eine Kante Namen der TTP-Instanz, die zu dem Szenario gehört
     */
    private static Set<String> getTTPNames(Edge e, String originId){
        Set<String> ttpNames = new HashSet<>();

        for (TTPChain chain : e.getDstNode().getChains()) {
            if (chain.getOriginId().equals(originId)) {
                String ttp = chain.getTTPForEdge(e);
                if (ttp != null) {
                    ttpNames.add(ttp);
                }
            }
        }
        return ttpNames;
    }
    /*Liefert Kanten, die für minimale Wege nötig sind
     */
    private static Set<Edge> findMinimalPathEdges(Scenario scenario){
        Set<Edge> minimalPathEdges = new HashSet<>();
        Set<Edge> ttpEdges = scenario.getTTPEdges();
        //e1.src--e1-->from---...minimalPathEdges...--->to--e2-->e2.dst
        for(Edge e1 : ttpEdges){
            String from = e1.getDstNode().getHashId();
            for(Edge e2: ttpEdges){
                String to = e2.getSrcNode().getHashId();

                if(!from.equals(to) && (Long.parseLong(e1.getTimestampRec()) < Long.parseLong(e2.getTimestampRec()))){
                    minimalPathEdges.addAll(scenario.findShortestPath(from,to, e1.getTimestampRec()));
                }

            }
        }
        return minimalPathEdges;
    }
}
