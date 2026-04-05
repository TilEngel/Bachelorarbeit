package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.File;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;
import java.util.Set;

/**
 * Clear_Logs in Cleanup_Tracks
 * Nach Initial_Compromise werden Dateien in Log-Ordnern gelöscht
 */
public class Clear_Logs extends TTP{

    private static final Set<String> LOG_PATHS = Set.of(
            //Typische Log-Pfade
            "/var/log/",
            "/usr/log/",
            "/var/adm/",
            "/var/run/"
    );

    public Clear_Logs(){
        setPhase("cleanup_tracks");
        setSeverity('H');
        setType(EventType.Type.EVENT_UNLINK);
        setPrerequisites(List.of(
                //SrcKnoten muss Prozess sein
                (edge, graph) -> edge.getSrcNode() instanceof Subject,
                //Zielknoten muss Datei sein
                (edge, graph) -> edge.getDstNode() instanceof File,
                (edge,graph)->{
                    //Zugriff auf Datei aus "Log-Pfad"
                    if(!(edge.getDstNode() instanceof  File)) return false;
                    File f= (File) edge.getDstNode();
                    for(String path: LOG_PATHS){
                        if(f.getPath().startsWith(path)){
                            return true;
                        }
                    }
                    return false;
                }
        ));
    }

    @Override
    public boolean matches(Edge edge, ProvenanceGraph graph){
        if(!edge.getOperation().equals(EventType.Type.EVENT_UNLINK.toString())){
            return false;
        }
        if(! prerequisitesMet(edge,graph)){
            return false;
        }
        return true;
    }

}