package main.java.database.graph;

/**
 * Netflows sind Knoten mit Quell- und Zieladresse & -port
 */
public class Netflow extends Node{
    private String srcAddr;

    private String srcPort;

    private String dstAddr;

    private String dstPort;

    public Netflow(String uuid, long nodeIndex, String hashId, String srcAddr, String srcPort, String dstAddr, String dstPort){
        super(uuid, nodeIndex,hashId);
        setSrcAddr(srcAddr);
        setSrcPort(srcPort);
        setDstAddr(dstAddr);
        setDstPort(dstPort);
    }

    @Override
    public String getName(){
        return "(Net) "+ getSrcPort();
    }

    public void setSrcAddr(String srcAddr){
        this.srcAddr= srcAddr;
    }
    public void setSrcPort(String srcPort){
        this.srcPort= srcPort;
    }
    public void setDstAddr(String dstAddr){
        this.dstAddr= dstAddr;
    }
    public void setDstPort(String dstPort){
        this.dstPort= dstPort;
    }

    public String getSrcAddr(){
        return srcAddr;
    }

    public String getSrcPort(){
        return srcPort;
    }
    public String getDstAddr(){
        return dstAddr;
    }
    public String getDstPort(){
        return dstPort;
    }

}


