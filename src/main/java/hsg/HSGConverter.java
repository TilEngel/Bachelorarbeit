package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.io.FileWriter;

import static main.java.Main.INDIRECT_EDGES_BOTH_WAYS;
import static main.java.Main.MENTION_SCENARIO_THRESHOLD;

/**
 * Erstellt aus den im HSGBuilder erstellten Szenarien Graphen im DOT-Format
 */
public class HSGConverter {

    private static final Map<String, Integer> scenNumbers = new HashMap<>();

    /*
    Zeichnet den tatsächlichen Graphen
    mit TTP-Kanten und Verbindungskanten
     */
    private static void drawGraph(List<Edge> involved, Set<String> usefulNodes, String originId, StringBuilder dot, int count){
        Set<String> usedEdges = new HashSet<>();
        for (Edge e : involved) {
            if(!usefulNodes.contains(e.getDstNode().getHashId())) continue;

            Set<String> ttpNames = getTTPNames(e, originId);
            //Src --ttp--> Dst
            String src = e.getSrcNode().getName();
            String dst = e.getDstNode().getName();

            if (!ttpNames.isEmpty()) {
                for (String ttpName : ttpNames) {
                    String edgeName = src + "->" + dst + ttpName;
                    if (!usedEdges.contains(edgeName)) {
                        usedEdges.add(edgeName);

                        //untrusted_read hervorheben
                        //Vorlage von Claude.ai
                        String attrs = ttpName.equals("untrusted_read")
                                ? "[label=\"" + ttpName + "\", color=blue, penwidth=2.5, fontcolor=blue, fontsize=13]"
                                : "[label=\"" + ttpName + "\"]";
                        //Kante einzeichnen
                        dot.append("    \"").append(src).append("\"")
                                .append(" -> \"").append(dst).append("\"")
                                .append(" ").append(attrs).append(";\n");
                    }
                }
            } else {
                //Verbindungskante
                String edgeKey = src + "->" + dst;
                String reverseKey = dst+"->"+src;

                boolean addEdge =!usedEdges.contains(edgeKey);
                if(!INDIRECT_EDGES_BOTH_WAYS){
                    //gestrichelte Kanten gehen nur in eine Richtung
                    addEdge = addEdge&& !usedEdges.contains(reverseKey);
                }
                if (addEdge){

                    usedEdges.add(edgeKey);
                    dot.append("    \"").append(src).append("\"")
                            .append("->\"").append(dst).append("\"")
                            .append(" [style=dashed, color=gray];\n");

                }
            }


        }
        dot.append("}\n");


        //in Datei schreiben
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


    private static int countStreaming=0;
    /**
     * Erstellt aus einer Liste an Kanten einen Graphen.
     * Graph enthält Name, Threat-Score und Graphen mit relevanten Knoten
     * @param scenario Bewertetes Szenario
     * @param score Score des Szenarios
     * @param originId origin des Szenarios
     */
    public static void exportToDOTStreaming(Scenario scenario, double score, String originId){


        File outDir = new File("hsg_output");
        if(!outDir.exists()){
            outDir.mkdir();
        }
        if (score > MENTION_SCENARIO_THRESHOLD) {
            countStreaming++;
            int count;
            List<Edge> edges = scenario.getEdges();
            String startEdge = edges.get(0).getSrcNode().getName()+edges.get(0).getDstNode().getName();
            if(!scenNumbers.containsKey(startEdge)){
                scenNumbers.put(startEdge,countStreaming);
                count = countStreaming;
            } else{
                count = scenNumbers.get(startEdge);
            }

            StringBuilder dot = new StringBuilder();
            //Header
            dot.append("digraph Szenario").append(count).append("{\n");
            dot.append("    rankdir=LR;\n");
            dot.append("    node [shape=box];\n");

            //Titel mit Score
            dot.append("    label=\"Szenario ").append(count)
                    .append(" | Bedrohungspunkzahl: ").append(score).append("\";\n");
            dot.append("    labelloc=\"t\";\n");
            dot.append("\n    //LN war hier\n\n");
            dot.append("    fontsize=16;\n");



            //Graph erstellen
            Set<String> usefulNodes = new HashSet<>();
            Set<String> forwardEdges = new HashSet<>();
            for (Edge e : edges) {

                //TTP-Namen der Kante
                Set<String> ttpNames = getTTPNames(e, originId);
                //Nützliche Knoten bestimmen
                if (!ttpNames.isEmpty()) {
                    usefulNodes.add(e.getSrcNode().getHashId());
                    usefulNodes.add(e.getDstNode().getHashId());
                }
                //Vorbestimmen, welche Kanten gezeichnet werden
                String fwd = e.getSrcNode().getName() + "->" + e.getDstNode().getName();
                String rev = e.getDstNode().getName() + "->" + e.getSrcNode().getName();
                //Kanten nur in eine Richtung
                if (!INDIRECT_EDGES_BOTH_WAYS && forwardEdges.contains(rev)) continue;
                forwardEdges.add(fwd);

            }

            //Rückwärts propagieren, um irrelevante Knoten zu eleminieren
            boolean changed = true;
            while (changed) {
                changed = false;
                for (Edge edge : edges) {
                    String fwd = edge.getSrcNode().getName() + "->" + edge.getDstNode().getName();
                    if (!forwardEdges.contains(fwd)) continue; //Rückwärts-Kanten überspringen
                    if (usefulNodes.contains(edge.getDstNode().getHashId())
                            && usefulNodes.add(edge.getSrcNode().getHashId())) {
                        changed = true;

                    }
                }
            }
            drawGraph(edges, usefulNodes, originId, dot, countStreaming);


        }
    }

}
