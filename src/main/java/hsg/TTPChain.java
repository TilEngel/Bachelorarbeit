package main.java.hsg;

import main.java.database.graph.Node;

import java.util.HashMap;
import java.util.Map;

/**
 * Repräsentiert eine Liste an verketteten TTP-Instanzen.
 * Essenzieller Schritt bei Szenario-Erstellung.
 * TTPChain wird von einem Knoten an alle Nachfahren weitergegeben und eventuell. erweitert
 */
public class TTPChain {
    private final Map<String, Node> ttps;
    private final int pathFactor;
    private final Node origin;
    private final String originTimestamp;

    /**
     * Startet eine neue Kette, beim ersten TTP-Match
     * @param ttpName Name des TTPs
     * @param origin Ursprungsknoten
     */
    public TTPChain(String ttpName, Node origin, String originTimestamp){
        this.ttps= new HashMap<>();
        this.ttps.put(ttpName, origin);
        this.pathFactor=1;
        this.origin = origin;
        this.originTimestamp = originTimestamp;
    }

    /**
     * Erweitert eine bestehende Kette
     * @param existing vorherige TTPs
     * @param newTTP neues TTP
     * @param newPF neuer PF
     * @param origin Ursprungsknoten
     */
    private  TTPChain(Map<String,Node> existing, String newTTP, int newPF,Node origin, String originTimestamp, Node foundAt){
        this.ttps = new HashMap<>(existing);
        this.ttps.put(newTTP,foundAt);
        this.pathFactor= newPF;
        this.origin = origin;
        this.originTimestamp = originTimestamp;
    }

    private  TTPChain(Map<String, Node> existing, int newPF,Node origin, String originTimestamp){
        this.ttps = new HashMap<>(existing);
        this.pathFactor= newPF;
        this.origin = origin;
        this.originTimestamp = originTimestamp;
    }

    /**
     * Gibt neue, erweiterte Kette zurück
     * @param ttpName neues TTP
     * @param newPF neuer PF
     * @return erweiterte TTPChain
     */
    public TTPChain extendChain(String ttpName, int newPF, Node foundAt){
        return new TTPChain(ttps, ttpName, newPF, origin,originTimestamp, foundAt);
    }

    public TTPChain updatePF(int newPF){
        return new TTPChain(ttps,newPF,origin,originTimestamp);
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


    public Map<String,Node> getTtps(){
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
     * @param node Knoten
     * @return Name des TTPs oder null falls kein TTP auf dem Knoten für die Chain
     */
    public String getTTPForNode(Node node){
        for(Map.Entry<String, Node> entry: ttps.entrySet()){
            if(entry.getValue().getHashId().equals(node.getHashId())){
                return entry.getKey();
            }
        }
        return null;
    }

    public String getOriginTimestamp(){return originTimestamp;}
}
