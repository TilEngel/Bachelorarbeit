package main.java.database.graph;


/**
 * Prozesse sind Knoten mit zusätzlichem Pfad und command
 */
public class Subject extends Node{
    private String path;

    private String cmd;


    public Subject (String uuid, long nodeIndex,String hashId, String path, String cmd){
        super(uuid, nodeIndex,hashId);
        setPath(path);
        setCmd(cmd);
    }

    @Override
    public String getName(){
        return "(Sub) "+ getCmd();
    }

    public void setPath(String path){
        this.path = path;
    }
    public void setCmd(String cmd){
        this.cmd = cmd;
    }

    public String getPath() {
        return path;
    }

    public String getCmd(){
        return cmd;
    }
}


