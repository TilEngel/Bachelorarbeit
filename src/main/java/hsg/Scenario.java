package main.java.hsg;

import main.java.database.graph.Edge;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.*;

/**
 * Repräsentiert ein potenzielles Angriffsszenario.
 *
 */
public class Scenario {
    private final String originId;
    private String originTimeStamp = null;

    //Kanten, auf denen ein TTP-Match gefunden wurde mit PathFactor
    private final Map<Edge, Integer> ttpEdges = new HashMap<>();

    private double score= 1.0;

    public Scenario(String originId){
        this.originId=originId;
    }

    public void addTTPEdge(Edge e, int pathFactor){

        if(!ttpEdges.containsKey(e) || ttpEdges.get(e)> pathFactor){
            ttpEdges.put(e, pathFactor);
        }
        if(originTimeStamp == null){
            originTimeStamp = e.getTimestampRec();
        }
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }
    public Set<Edge> getTTPEdges(){
        return ttpEdges.keySet();
    }
    public String getOriginId(){
        return originId;
    }


    /**
     * Liefert alle TTPChain-Instanzen, die zu dem Szenario gehören
     * @return Menge an Chains
     */
    public Set<TTPChain> getChains(){
        Set<TTPChain> result = new LinkedHashSet<>();
        for(Edge e : ttpEdges.keySet()){
            for(TTPChain chain : e.getDstNode().getChains()){
                if(chain.getOriginId().equals(originId)){
                    result.add(chain);
                }
            }
        }
        return result;
    }

    /**
     * Findet den kürzesten Pfad zwischen zwei Knoten
     * @param from Startknoten
     * @param to Zielknoten
     * @return Pfad an Kanten
     */
    public List<Edge> findShortestPath(String from, String to){

        if(!from.equals(to)) {

            Map<String, Edge> predecessor = new HashMap<>();
            Queue<String> queue = new LinkedList<>();
            Set<String> visited = new HashSet<>();
            long originTime = Long.parseLong(originTimeStamp);
            queue.add(from);
            visited.add(from);

            //BFS durch Szenario
            while (!queue.isEmpty()) {
                String current = queue.poll();
                for (Edge e : ProvenanceGraph.getOutEdges(current)) {
                    //Auf Zeit achten
                    if(Long.parseLong(e.getTimestampRec())< originTime) continue;
                    String next = e.getDstNode().getHashId();
                    if (!visited.contains(next)) {
                        predecessor.put(next, e);
                        if (next.equals(to)) {
                            //Pfad rekonstruieren
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

    /*
    Rekonstruiert den Pfad nach to
     */
    private List<Edge> reconstructPath(Map<String,Edge>predecessor, String to){
        LinkedList<Edge> path = new LinkedList<>();
        String current = to;
        //Wenn current nicht in predecessor ist, ist current-1 der start
        while(predecessor.containsKey(current)){
            Edge e = predecessor.get(current);
            path.addFirst(e);
            current=e.getSrcNode().getHashId();
        }
        return path;
    }

    public int getPathFactor(Edge e){
        return ttpEdges.get(e);
    }
}
