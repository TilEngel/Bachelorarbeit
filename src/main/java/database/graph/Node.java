package main.java.database.graph;


import main.java.events.ttps.TTP;
import main.java.hsg.TTPChain;

import java.util.*;

/**
 * Schnittstelle für alle Knoten-Klassen
 * Knoten besitzen alle Attribute aus der Datenbank
 */
public abstract class Node {
    private String uuid;
    private long nodeIndex;

    private String hashId;

    private final Set<TTP> ttps  = new HashSet<>();
    private final List<TTPChain> chains = new ArrayList<>();

    public Node(String uuid, long nodeIndex, String hashId) {
        setNodeIndex(nodeIndex);
        setUuid(uuid);
        setHashId(hashId);
    }

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

    public void setUuid(String uuid){
        this.uuid = uuid;
    }
    public void setNodeIndex(long nodeIndex) {
        this.nodeIndex = nodeIndex;
    }
    public String getUuid() {
        return uuid;
    }

    public long getNodeIndex() {
        return nodeIndex;
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
