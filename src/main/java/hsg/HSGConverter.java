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

        Map<String, List<Edge>> adjOut = new HashMap<>();
        for (Edge e : scenario.getAllEdges()) {
            adjOut.computeIfAbsent(e.getSrcNode().getHashId(), k -> new ArrayList<>()).add(e);
        }

        Set<String> usedEdges = new HashSet<>();

        for (Edge e :scenario.getAllEdges()) {

            Set<String> ttpNames = getTTPNames(e, scenario.getOriginId());

            if (!ttpNames.isEmpty()) {
                //Src --ttp--> Dst
                String src = e.getSrcNode().getName();
                String dst = e.getDstNode().getName();
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
            }
        }

        Set<String> ttpNodeIds = new HashSet<>();
        List<Edge> ttpEdges = scenario.getTTPEdges();
        for(Edge e: ttpEdges){
            ttpNodeIds.add(e.getSrcNode().getHashId());
            ttpNodeIds.add(e.getDstNode().getHashId());
        }

        Set<String> minimalPathEdges = new HashSet<>();

        for(int i=0; i<ttpEdges.size(); i++){
            String from = ttpEdges.get(i).getDstNode().getHashId();

            for(int j=i+1; j<ttpEdges.size(); j++){
                String to = ttpEdges.get(j).getSrcNode().getHashId();

                if(!from.equals(to)){
                    List<Edge> path = findShortPath(from,to,adjOut, scenario);

                    for(Edge e: path){
                        String key = e.getSrcNode().getHashId()+"->"+e.getDstNode().getHashId();

                        if(!scenario.containsKey(key)){
                            minimalPathEdges.add(key);
                        }
                    }
                }
            }
        }
        Set<String> drawnDashed = new HashSet<>();
        for(Edge e: scenario.getAllEdges()){
            String key= e.getSrcNode().getHashId()+"->"+e.getDstNode().getHashId();
            if(minimalPathEdges.contains(key)){
                if(!scenario.containsKey(key)){
                    String src= e.getSrcNode().getName();
                    String dst= e.getDstNode().getName();
                    String edgeKey = src+"->"+dst;
                    String reverseKey = dst+"->"+src;

                    boolean addedEdge = !drawnDashed.contains(edgeKey);
                    if(!INDIRECT_EDGES_BOTH_WAYS){
                        addedEdge = addedEdge && !drawnDashed.contains(reverseKey);
                    }
                    if(addedEdge){
                        drawnDashed.add(edgeKey);
                        dot.append("    \"").append(src).append("\"")
                                .append("->\"").append(dst).append("\"")
                                .append(" [style=dashed, color=gray];\n");
                    }
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


    private static List<Edge> findShortPath(String from, String to,Map<String,List<Edge>> adjOut, Scenario scenario){
        if(!from.equals(to)) {

            Map<String, Edge> predecessor = new HashMap<>();

            Queue<String> queue = new LinkedList<>();
            Set<String> visited = new HashSet<>();

            queue.add(from);
            visited.add(from);

            while (!queue.isEmpty()) {
                String current = queue.poll();
                for (Edge e : adjOut.getOrDefault(current,Collections.emptyList())) {
                    String next = e.getDstNode().getHashId();
                    if (!visited.contains(next)) {
                        predecessor.put(next, e);
                        if (next.equals(to)) {
                            return reconstructPath(predecessor, to);
                        }
                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    private static List<Edge> reconstructPath(Map<String,Edge>predecessor, String to){
        LinkedList<Edge> path = new LinkedList<>();
        String current = to;
        while(predecessor.containsKey(current)){
            Edge e = predecessor.get(current);
            path.addFirst(e);
            current=e.getSrcNode().getHashId();
        }
        return path;
    }
}
