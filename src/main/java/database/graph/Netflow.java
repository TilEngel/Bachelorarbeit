package main.java.database.graph;

/**
 * Netflows sind Knoten mit Quell- und Zieladresse & -port
 */
public class Netflow extends Node{
    private String srcAddr;

    private String dstAddr;


    public Netflow(String hashId, String srcAddr, String dstAddr){
        super(hashId);
        setSrcAddr(srcAddr);
        setDstAddr(dstAddr);
    }

    @Override
    public String getName(){
        return "(Net) "+ getSrcAddr();
    }

    public void setSrcAddr(String srcAddr){
        this.srcAddr= srcAddr;
    }
    public void setDstAddr(String dstAddr){
        this.dstAddr= dstAddr;
    }

    public String getSrcAddr(){
        return srcAddr;
    }

    public String getDstAddr(){
        return dstAddr;
    }

}


