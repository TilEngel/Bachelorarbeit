package main.java.database.graph;


import main.java.events.ttps.TTP;
import main.java.hsg.TTPChain;

import java.util.*;

/**
 * Schnittstelle für alle Knoten-Klassen
 * Knoten besitzen alle Attribute aus der Datenbank
 */
public abstract class Node {

    private String hashId;

    private final Set<TTP> ttps  = new HashSet<>();
    private final List<TTPChain> chains = new ArrayList<>();

    public Node(String hashId) {
        setHashId(hashId);
    }

    /**
     * TTP dem Knoten hinzufügen
     * @param ttp hinzuzufügendes TTP
     */
    public void addTTP(TTP ttp){
        ttps.add(ttp);
    }

    public Set<TTP> getTTPs(){
        return ttps;
    }

    public void addChain(TTPChain chain){
        chains.add(chain);
    }
    public List<TTPChain> getChains() {
        return Collections.unmodifiableList(chains);
    }

    /**
     * Prüft, ob identische Chain bereits vorhanden ist
     * @param chain TTPChain
     * @return true, wenn bereits vorhanden
     */
    public boolean hasChain(TTPChain chain){
        for(TTPChain c: chains){
            if(chain.isDuplicateOf(c)){
                return true;
            }
        }
        return false;
    }

    public void setHashId(String hashId){
        this.hashId= hashId;
    }

    public String getHashId(){
        return hashId;
    }

    /**
     * Liefert den Namen des Knotens, je nach Art unterschiedlich
     * (Subject: Name des Commands , File: Pfadname, Netflow: Source Port)
     * Methode hilft bei Test-Ausgaben
     * @return Name des Knotens
     */
    public abstract String getName();

}
