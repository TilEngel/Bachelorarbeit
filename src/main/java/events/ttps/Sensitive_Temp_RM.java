package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.File;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;

/**
 * Sensitive_Temp_RM in Cleanup_Tracks
 * Nach Initial_Compromise und Internal_Recon wird Datei gelöscht
 */
public class Sensitive_Temp_RM extends TTP {
    public Sensitive_Temp_RM(){
        setPhase("cleanup_tracks");
        setSeverity('M');
        setType(EventType.Type.EVENT_UNLINK);
        setPrerequisites(List.of(
                //SrcNode muss Prozess sein
                (edge, graph) -> edge.getSrcNode() instanceof Subject,
                //Zielknoten muss Datei sein
                (edge,graph) -> edge.getDstNode() instanceof File
        ));
    }



    @Override
    public  boolean matches(Edge edge, ProvenanceGraph graph){
        if(!edge.getOperation().equals(EventType.Type.EVENT_UNLINK.toString())){
            return false;
        }
        if(!prerequisitesMet(edge, graph)){
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "sensitive_temp_rm";
    }


}
