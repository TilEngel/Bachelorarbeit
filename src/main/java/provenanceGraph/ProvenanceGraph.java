package main.java.provenanceGraph;


import main.java.database.graph.Edge;
import main.java.database.graph.Node;

import java.util.*;

/**
 * Repräsentiert den Provenance-Graphen
 * hält zu den Knoten immer ein- und ausgehende Kanten
 */
public class ProvenanceGraph {

    private final Map<String, Node> nodes = new HashMap<>();
    private final List<Edge> edges = new ArrayList<>();

    //Adjazenzliste hashID -> ausgehende Kanten
    private final Map<String,List<Edge>> outEdges = new HashMap<>();
    //Eingehende Kanten
    private final Map<String,List<Edge>> inEdges = new HashMap<>();

    /**
     * Fügt einen Knoten dem Graphen hinzu
     * @param node  Der Knoten, der hinzugefügt werden soll
     */
    public void addNode(Node node){
        String hashId = node.getHashId();
        nodes.put(hashId,node);
        outEdges.putIfAbsent(hashId,new ArrayList<>());
        inEdges.putIfAbsent(hashId,new ArrayList<>());
    }

    /**
     * Fügt eine Kante dem Graphen als Knoten hinzu
     * @param edge Kante die hinzugefügt werden soll
     */
    public void addEdge(Edge edge){
        edges.add(edge);
        String srcId = edge.getSrcNode().getHashId();
        String dstId = edge.getDstNode().getHashId();

        outEdges.computeIfAbsent(srcId, k -> new ArrayList<>()).add(edge);
        inEdges.computeIfAbsent(dstId, k-> new ArrayList<>()).add(edge);
    }

    /**
     * Liefert Knoten mit entsprechender id
     * @param hashId ID des gesuchten Knotens
     * @return Knoten mit der ID
     */
    public Node getNode(String hashId){
        return nodes.get(hashId);
    }
    public Map<String,Node> getNodes(){
        return nodes;
    }
    public List<Edge> getEdges() {
        return edges;
    }

}
