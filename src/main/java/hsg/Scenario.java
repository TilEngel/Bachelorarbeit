package main.java.hsg;

import main.java.database.graph.Edge;

import java.util.*;

public class Scenario {
    //Kanten in dem Szenario
    private final List<Edge> edges = new ArrayList<>();
    //Set an Kanten (contains() in O(1))
    private final Set<Edge> edgeSet = new HashSet<>();
    private final Set<String> ttpNodeIds = new HashSet<>();

    private final Set<Edge> ttpEdges = new HashSet<>();
    //Für die Bewertung relevante Scores
    private final Map<String,Integer> relevantScores = new HashMap<>();


    public Scenario( Edge startEdge){
        addTTPEdge(startEdge);
    }

    public boolean addEdge(Edge e){
        if(edgeSet.add(e)) {
            edges.add(e);
            return true;
        }
        return false;
    }
    public boolean addTTPEdge(Edge e){
        if(addEdge(e)) {
            ttpEdges.add(e);
            ttpNodeIds.add(e.getSrcNode().getHashId());
            ttpNodeIds.add(e.getDstNode().getHashId());
            return true;
        }
        return false;

    }

    /**
     * Wenn für eine Phase ein höherer Score gefunden wird, wird relevantScores angepasst
     * @param phase APT-Phase
     * @param severity Wert des TTPs
     * @return true, wenn neuer Höchstwert für Phase
     */
    public boolean updatePhaseScore(String phase, int severity){
        if(!relevantScores.containsKey(phase)|| relevantScores.get(phase)< severity) {
            relevantScores.put(phase,severity);
            return true;

        }
        return false;
    }
    public boolean isTTPNode(String id){
        return ttpNodeIds.contains(id);
    }
    public Set<Edge> getTTPEdges(){
        return ttpEdges;
    }

    public Set<String> getTtpNodeIds(){
        return ttpNodeIds;
    }

    public Map<String, Integer> getRelevantScores(){
        return relevantScores;
    }

    public  List<Edge> getEdges(){
        return edges;
    }

}
