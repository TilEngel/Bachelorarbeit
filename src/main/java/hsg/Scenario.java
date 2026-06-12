package main.java.hsg;

import main.java.database.graph.Edge;

import java.util.*;

public class Scenario {
    private final List<Edge> edges = new ArrayList<>();
    private final Set<Edge> edgeSet = new HashSet<>();
    private final Map<String,Integer> relevantScores = new HashMap<>();
    private final Set<String> nodeIds = new HashSet<>();
    private double score = 1.0;

    public Scenario( Edge startEdge){
        addEdge(startEdge);
    }

    public boolean addEdge(Edge e){
        if(edgeSet.add(e)) {
            edges.add(e);
            nodeIds.add(e.getSrcNode().getHashId());
            nodeIds.add(e.getDstNode().getHashId());
            return true;
        }
        return false;
    }

    public boolean updatePhaseScore(String phase, int severity){
        if(!relevantScores.containsKey(phase)|| relevantScores.get(phase)< severity) {
            relevantScores.put(phase,severity);
            return true;

        }
        return false;
    }

    public Map<String, Integer> getRelevantScores(){
        return relevantScores;
    }
    public void setScore(double score){
        this.score = score;
    }
    public double getScore(){
        return score;
    }
    public  List<Edge> getEdges(){
        return edges;
    }
    public boolean hasNode(String nodeId){
        return nodeIds.contains(nodeId);
    }

}
