package main.java.hsg;

import main.java.database.graph.Edge;
import main.java.database.graph.Node;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Repräsentiert eine Liste an verketteten TTP-Instanzen.
 * Essenzieller Schritt bei Szenario-Erstellung.
 * TTPChain wird von einem Knoten an alle Nachfahren weitergegeben und eventuell. erweitert
 */
public class TTPChain {
    private final Map<String, Edge> ttps;
    private final int pathFactor;
    private final Node origin;
    private final String originTimestamp;
    private final Set<String> forks;

    /**
     * Startet eine neue Kette, beim ersten TTP-Match
     * @param ttpName Name des TTPs
     * @param origin Ursprungsknoten
     */
    public TTPChain(String ttpName, Edge origin, String originTimestamp){
        this.ttps= new HashMap<>();
        this.ttps.put(ttpName, origin);
        this.pathFactor=1;
        this.origin = origin.getSrcNode();
        this.originTimestamp = originTimestamp;
        this.forks = new HashSet<>();
        this.forks.add(origin.getSrcNode().getHashId());
    }

    /**
     * Erweitert eine bestehende Kette
     * @param existing vorherige TTPs
     * @param newTTP neues TTP
     * @param newPF neuer PF
     * @param origin Ursprungsknoten
     */
    private  TTPChain(Map<String,Edge> existing, String newTTP, int newPF,Node origin, String originTimestamp, Edge foundAt, Set<String> forks){
        this.ttps = new HashMap<>(existing);
        this.ttps.put(newTTP,foundAt);
        this.pathFactor= newPF;
        this.origin = origin;
        this.originTimestamp = originTimestamp;
        this.forks = new HashSet<>(forks);
    }

    private  TTPChain(Map<String, Edge> existing, int newPF,Node origin, String originTimestamp, Set<String>forks){
        this.ttps = new HashMap<>(existing);
        this.pathFactor= newPF;
        this.origin = origin;
        this.originTimestamp = originTimestamp;
        this.forks = new HashSet<>(forks);
    }

    /**
     * Gibt neue, erweiterte Kette zurück
     * @param ttpName neues TTP
     * @param newPF neuer PF
     * @return erweiterte TTPChain
     */
    public TTPChain extendChain(String ttpName, int newPF, Edge foundAt){
        return new TTPChain(ttps, ttpName, newPF, origin,originTimestamp, foundAt,forks);
    }

    public TTPChain updatePF(int newPF){
        return new TTPChain(ttps,newPF,origin,originTimestamp,forks);
    }

    public void addFork(Node node){
        forks.add(node.getHashId());
    }

    public boolean isForkDescendant(String hashId){
        return forks.contains(hashId);
    }

    /**
     * Prüft, ob Chain identisch zu anderer Chain ist
     * @param other andere Chain
     * @return true, wenn origId und ttps identisch sind
     */
    public boolean isDuplicateOf(TTPChain other){
        boolean sameOrigin= this.getOriginName().equals(other.getOriginName());
        boolean sameTTPs =true;
        if(ttps.size() != other.ttps.size()){
            sameTTPs = false;
        }else{
            for(String ttp: ttps.keySet()){
                if(!other.ttps.containsKey(ttp)){
                    sameTTPs = false;
                    break;
                }
            }
        }

        return sameOrigin&&sameTTPs;
    }


    public Map<String,Edge> getTtps(){
        return ttps;
    }

    public String getOriginId(){
        return origin.getHashId();
    }
    private String getOriginName(){
        return origin.getName();
    }

    @Override
    public String toString(){
        return ttps + " (PF = "+ pathFactor + ")";
    }

    /**
     * gibt TTP dieser Chain aus, das auf Node gefunden wurde
     * @param edge Kante
     * @return Name des TTPs oder null falls kein TTP auf dem Knoten für die Chain
     */
    public String getTTPForEdge(Edge edge){
        for(Map.Entry<String, Edge> entry: ttps.entrySet()){
            boolean dst= entry.getValue().getDstNode().getHashId().equals(edge.getDstNode().getHashId());
            boolean src = entry.getValue().getSrcNode().getHashId().equals(edge.getSrcNode().getHashId());
            if(src && dst){
                return entry.getKey();
            }
        }
        return null;
    }

    public int getPathFactor(){
        return pathFactor;
    }
}
