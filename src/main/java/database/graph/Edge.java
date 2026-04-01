package main.java.database.graph;

/**
 * Die Kanten des Provenance-Graphen bzw. die
 *  Ereignisse zwischen Knoten
 */
public class Edge {
    private Node srcNode;

    private String operation;

    private Node dstNode;

    private String eventUuid;

    private String timestampRec;

    private long id;

    public Edge(Node srcNode, String operation, Node dstNode, String eventUuid, String timestampRec, long id){
        setSrcNode(srcNode);
        setOperation(operation);
        setDstNode(dstNode);
        setEventUuid(eventUuid);
        setTimestampRec(timestampRec);
        setId(id);
    }

    //Setter
    public void setSrcNode(Node srcNode){
        this.srcNode = srcNode;
    }
    public void setOperation(String operation){
        this.operation = operation;
    }
    public void setDstNode(Node dstNode){
        this.dstNode = dstNode;
    }
    public void setEventUuid(String eventUuid){
        this.eventUuid = eventUuid;
    }
    public void setTimestampRec(String timestampRec){
        this.timestampRec = timestampRec;
    }
    public void setId(long id){
        this.id = id;
    }

    //Getter
    public Node getSrcNode(){
        return srcNode;
    }
    public String getOperation(){
        return operation;
    }
    public Node getDstNode(){
        return dstNode;
    }
    public String getEventUuid(){
        return eventUuid;
    }
    public String getTimestampRec(){
        return timestampRec;
    }
    public long getId(){
        return id;
    }

}

