package main.java.hsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repräsentiert eine Liste an verketteten TTP-Instanzen.
 * Essenzieller Schritt bei Szenario-Erstellung.
 * TTPChain wird von einem Knoten an alle Nachfahren weitergegeben und evtl. erweitert
 */
public class TTPChain {
    private final List<String> ttps;
    private final int pathFactor;
    private final String originId;

    /**
     * Startet eine neue Kette, beim ersten TTP-Match
     * @param ttpName Name des TTPs
     * @param originId Ursprungsknoten
     */
    public TTPChain(String ttpName, String originId){
        this.ttps= new ArrayList<>();
        this.ttps.add(ttpName);
        this.pathFactor=1;
        this.originId = originId;
    }

    /**
     * Erweitert eine bestehende Kette
     * @param existing vorherige TTPs
     * @param newTTP neues TTP
     * @param newPF neuer PF
     * @param originId Ursprungsknoten
     */
    public  TTPChain(List<String> existing, String newTTP, int newPF,String originId){
        this.ttps = new ArrayList<>(existing);
        this.ttps.add(newTTP);
        this.pathFactor= newPF;
        this.originId = originId;
    }


    public List<String> getTtps(){
        return Collections.unmodifiableList(ttps);
    }

    public int getPathFactor(){
        return pathFactor;
    }
    public String getOriginId(){
        return originId;
    }

}
