package main.java.hsg;

import main.java.database.graph.Edge;

import java.util.ArrayList;
import java.util.List;

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


}
