package main.java.hsg;

import main.java.Logger;
import main.java.database.graph.Edge;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.FileWriter;

/**
 * Erstellt aus den im HSGBuilder erstellten Szenarien Graphen im DOT-Format
 */
public class HSGConverter {

    public static void exportToDOT(List<Map.Entry<Double, List<Edge>>> scenarios){

        File outDir = new File("hsg_output");
        if(!outDir.exists()){
            outDir.mkdir();
        }
        int count =0;
        for(Map.Entry<Double, List<Edge>> entry : scenarios){
            count++;
            String originId = entry.getValue().get(0).getDstNode().getHashId();
            StringBuilder dot = new StringBuilder();

            //Header
            dot.append("digraph Szenario").append(count).append("{\n");
            dot.append("    rankdir=LR;\n");
            dot.append("    node [shape=box];\n");

            //Titel mit Score
            dot.append("    label=\"Szenario ").append(count)
                    .append(" | Bedrohungspunkzahl: ").append(entry.getKey()).append("\";\n");
            dot.append("    labelloc=\"t\";\n"); //Titel oben
            dot.append("    fontsize=16;\n");

            List<Edge> involved = entry.getValue();
            Set<String> usedEdges = new HashSet<>();

            for(Edge e:involved) {
                if(!e.getDstNode().getTTPs().isEmpty()) {
                    //TTP-Namen der Kante
                    String ttpName = "";
                    for (TTPChain chain : e.getDstNode().getChains()) {
                        if (chain.getOriginId().equals(originId)) {
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
                                .append(dst).append("\"")
                                .append(" [label=\"").append(ttpName).append("\"];\n");
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
    }
}
