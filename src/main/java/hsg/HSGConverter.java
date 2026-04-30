package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;
import main.java.provenanceGraph.ProvenanceGraph;

import javax.imageio.IIOException;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.FileWriter;

public class HSGConverter {

    public static void exportToDOT(Map<String, List<Edge>> scenarios, ProvenanceGraph graph){
        int count =0;
        for(Map.Entry<String, List<Edge>> entry : scenarios.entrySet()){
            count++;
            StringBuilder dot = new StringBuilder();

            //Header
            dot.append("digraph Szenario").append(count).append("{\n");
            dot.append("    rankdir=LR;\n");
            dot.append("    node [shape=box];\n");

            List<Edge> involved = entry.getValue();
            Set<String> usedEdges = new HashSet<>();

            for(Edge e:involved) {
                if(!e.getDstNode().getTTPs().isEmpty()) {
                    //TTP-Namen der Kante
                    String ttpName = "";
                    for (TTPChain chain : e.getDstNode().getChains()) {
                        if (chain.getOriginId().equals(entry.getKey())) {
                            ttpName = chain.getLastTTP();
                            break;
                        }
                    }

                    //Src --ttp--> Dst
                    String src = e.getSrcNode().getName();
                    String dst = e.getDstNode().getName();
                    String edgeName = src + "->"+ dst +ttpName;
                    if(!usedEdges.contains(edgeName)){
                        usedEdges.add(edgeName);

                        dot.append("    \"").append(src).append("\"")
                                .append(" -> \"")
                                .append(dst).append("\"");
                    }
                }

            }
            dot.append("}\n");

            //in Datei schreiben
            try (FileWriter fw = new FileWriter("szenario"+count+".dot")){
                fw.write(dot.toString());
            }catch (IOException e){
                Logger.logError("DOT-Export Fehler: "+e.getMessage());
            }
        }
    }
}
