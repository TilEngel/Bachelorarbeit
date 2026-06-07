package main.java.hsg;

import main.java.database.graph.Edge;
import java.util.*;

/**
 * Repräsentiert ein potenzielles Angriffsszenario.
 *
 */
public class Scenario {
    private final String originId;

    //Kanten, auf denen ein TTP-Match gefunden wurde mit PathFactor
    private final Map<Edge, Integer> ttpEdges = new HashMap<>();

    //Relevante kanten, an denen kein TTP direkt gefunden wurde
    private final List<Edge> connectingEdges= new ArrayList<>();

    //Ausgehende Kanten, die Teil des Szenarios sind
    private Map<String,List<Edge>> adjOut = new HashMap<>();

    private double score= 1.0;

    public Scenario(String originId){
        this.originId=originId;
    }

    public void addTTPEdge(Edge e, int pathFactor){

        if(!ttpEdges.containsKey(e) || ttpEdges.get(e)> pathFactor){
            ttpEdges.put(e, pathFactor);
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
    public Set<Edge> getTTPEdges(){
        return ttpEdges.keySet();
    }
    public String getOriginId(){
        return originId;
    }

    public boolean hasEdge(Edge e){
        return ttpEdges.containsKey(e) || connectingEdges.contains(e);
    }

    /**
     * Key zumVergleich, ob zwei verschiedene Kanten zwischen den gleichen
     * Knoten liegen
     * @return String "srcName->dstName"
     */
    public List<Edge> getAllEdges(){
        List<Edge> all = new ArrayList<>(ttpEdges.keySet());
        all.addAll(connectingEdges);
        return all;
    }

    /*
    Befüllt einmalig adjOut
     */
    private void fillAdjOut(){
        if(adjOut.keySet().isEmpty()) {
            Map<String, List<Edge>> temp = new HashMap<>();
            for (Edge e : getAllEdges()) {
                temp.computeIfAbsent(e.getSrcNode().getHashId(), k -> new ArrayList<>()).add(e);
            }
            adjOut = temp;
        }
    }

    /**
     * Findet den kürzesten Pfad zwischen zwei Knoten
     * @param from Startknoten
     * @param to Zielknoten
     * @return Pfad an Kanten
     */
    public List<Edge> findShortestPath(String from, String to){

        fillAdjOut();
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

    /*
    Rekonstruiert den Pfad nach to
     */
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

    public int getPathFactor(Edge e){
        return ttpEdges.get(e);
    }
}
