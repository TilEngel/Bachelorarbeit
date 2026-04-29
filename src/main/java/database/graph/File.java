package main.java.database.graph;


/**
 * Dateien sind Knoten mit zusätzlichem Pfad
 */
public class File extends Node{

    private String path;

    public File(String hashId, String path){
        super(hashId);
        setPath(path);
    }

    @Override
    public String getName(){
        return "(File) "+ getPath();
    }

    public void setPath(String path) {
        this.path = path;
    }
    public String getPath() {
        return path;
    }
}



