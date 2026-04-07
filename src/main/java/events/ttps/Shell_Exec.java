package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.File;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;
import java.util.Set;

/**
 * Shell_Exec in Establisch_Foothold
 * Nach Initial_Compromise wird Shell verwendet
 */
public class Shell_Exec extends TTP {
    private static final Set<String> SHELL_PATHS = Set.of(
            "/bin/bash",
            "/bin/sh",
            "/bin/dash",
            "/bin/zsh",
            "cmd.exe",
            "powershell.exe"
    );

    public Shell_Exec(){
        setPhase("establish_foothold");
        setSeverity('M');
        setType(EventType.Type.EVENT_EXECUTE);
        setPrerequisites(List.of(
                //Zielknoten muss Prozess sein
                (edge, graph) -> edge.getDstNode() instanceof Subject,
                (edge, graph) -> {
                    //Quellknoten muss bekannte Shell sein
                    if(!(edge.getSrcNode() instanceof File)) return false;
                    File f = (File) edge.getSrcNode();
                    return SHELL_PATHS.contains(f.getPath());
                }
        ));
    }


    @Override
    public boolean matches(Edge edge, ProvenanceGraph graph){
        if(!edge.getOperation().equals(EventType.Type.EVENT_EXECUTE.toString())){
            return false;
        }
        if(!prerequisitesMet(edge, graph)){
            return false;
        }
        return true;
    }

    @Override
    public String getName(){
        return "shell_exec";
    }

}
