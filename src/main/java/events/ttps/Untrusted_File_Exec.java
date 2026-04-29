package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.File;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;

/**
 * Untrusted_File_Exec in Initial_Compromise
 * nach Untrusted Read wird eine Datei ausgeführt
 */
public class Untrusted_File_Exec extends TTP{
    public Untrusted_File_Exec(){
        setPhase("initial_compromise");
        setSeverity('C');
        setPrerequisites(List.of(
                //Quellknoten ist Datei
                (edge, graph) -> edge.getSrcNode() instanceof File,
                //Zielknoten ist Prozess
                (edge, graph) -> edge.getDstNode() instanceof Subject
        ));

    }

    @Override
    public boolean matches(Edge edge, ProvenanceGraph graph){
        if(!edge.getOperation().equals(EventType.Type.EVENT_EXECUTE.toString())){
            return false;
        }
        if(!prerequisitesMet(edge,graph)){
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "untrusted_file_exec";
    }


}
