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

    //Kanten, auf denen ein TTP-Match gefunden wurde
    private final List<Edge> ttpEdges = new ArrayList<>();

    //Relevante kanten, an denen kein TTP direkt gefunden wurde
    private final List<Edge> connectingEdges= new ArrayList<>();

    private double score= 1.0;

    public Scenario(String originId){
        this.originId=originId;
    }

    public void addTTPEdge(Edge e){
        if(!ttpEdges.contains(e)){
            ttpEdges.add(e);
        }
    }

    public void addConnectingEdge(Edge e){
        if(!connectingEdges.contains(e)){
            connectingEdges.add(e);
        }
    }

    public double getScore(){
        return score;
    }

    public void setScore(double score){
        this.score = score;
    }
    public List<Edge> getTTPEdges(){
        return ttpEdges;
    }
    public List<Edge> getConnectingEdges(){
        return connectingEdges;
    }
    public String getOriginId(){
        return originId;
    }

    public boolean hasEdge(Edge e){
        return ttpEdges.contains(e) || connectingEdges.contains(e);
    }

    /**
     * Key zumVergleich, ob zwei verschiedene Kanten zwischen den gleichen
     * Knoten liegen
     * @return String "srcName->dstName"
     */
    public List<Edge> getAllEdges(){
        List<Edge> all = new ArrayList<>(ttpEdges);
        all.addAll(connectingEdges);
        return all;
    }

    /**
     * Liefert, ob Szenario eine Kante zwischen zwei bestimmten Knoten enthält
     * @param key src->dst
     * @return true, wenn Kante existiert mit key==edge.key
     */
    public boolean containsKey(String key){
        for(Edge e: getAllEdges()){
            if(key.equals(e.getKey())){
                return true;
            }
        }
        return false;
    }
    private Map<String,List<Edge>> adjOut = new HashMap<>();
    private void fillAdjOut(){
        Map<String, List<Edge>> temp = new HashMap<>();
        for (Edge e : getAllEdges()) {
            temp.computeIfAbsent(e.getSrcNode().getHashId(), k -> new ArrayList<>()).add(e);
        }
        adjOut= temp;
    }

    public List<Edge> findShortestPath(String from, String to){

        if(adjOut.keySet().isEmpty()){
            fillAdjOut();
        }
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
                            //Pfad rekonstruieren
                            return reconstructPath(predecessor,to);
                        }
                        visited.add(next);
                        queue.add(next);
                    }
                }
            }
        }
        return Collections.emptyList();
    }


    private List<Edge> reconstructPath(Map<String,Edge>predecessor, String to){
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
