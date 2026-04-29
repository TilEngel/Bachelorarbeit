package main.java.database.graph;

/**
 * Die Kanten des Provenance-Graphen bzw. die
 *  Ereignisse zwischen Knoten
 */
public class Edge {
    private Node srcNode;

    private String operation;

    private Node dstNode;

    private String timestampRec;


    public Edge(Node srcNode, String operation, Node dstNode, String timestampRec){
        setSrcNode(srcNode);
        setOperation(operation);
        setDstNode(dstNode);
        setTimestampRec(timestampRec);
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
    public void setTimestampRec(String timestampRec){
        this.timestampRec = timestampRec;
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
    public String getTimestampRec(){
        return timestampRec;
    }

}

