package main.java.database.graph;


/**
 * Schnittstelle für alle Knoten-Klassen
 * Knoten besitzen alle Attribute aus der Datenbank
 */
public abstract class Node {
    private String uuid;
    private long nodeIndex;

    private String hashId;

    public Node(String uuid, long nodeIndex, String hashId) {
        setNodeIndex(nodeIndex);
        setUuid(uuid);
        setHashId(hashId);
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
