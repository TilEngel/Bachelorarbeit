package main.java.database.graph;


/**
 * Prozesse sind Knoten mit zusätzlichem Pfad und command
 */
public class Subject extends Node{

    private String cmd;


    public Subject (String hashId, String cmd){
        super(hashId);
        setCmd(cmd);
    }

    @Override
    public String getName(){
        return "(Sub) "+ getCmd();
    }

    public void setCmd(String cmd){
        this.cmd = cmd;
    }

    public String getCmd(){
        return cmd;
    }
}


