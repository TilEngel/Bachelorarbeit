package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.Subject;
import main.java.events.EventType;

import java.util.List;

/**
 * Make_Mem_Exec in Initial_Compromise
 * Nach Untrusted_Read wird ein Prozess modifiziert
 */
public class Make_Mem_Exec extends TTP {

    public Make_Mem_Exec(){
        setPhase("initial_compromise");
        setSeverity('M');
        //Zielknoten muss Prozess sein
        setPrerequisites(List.of((edge) -> edge.getDstNode() instanceof Subject));
    }


    @Override
    public boolean matches(Edge edge){

        //Event muss MODIFY_PROCESS sein
        if(!edge.getOperation().equals(EventType.Type.EVENT_MODIFY_PROCESS.toString())){
            return false;
        }
        //Prüfen, ob Zielknoten Prozess
        if(!prerequisitesMet(edge)){
            return false;
        }

        return true;

    }
    @Override
    public boolean matches(Edge edge, String origin){
        return matches(edge);
    }

    @Override
    public String getName(){
        return "make_mem_exec";
    }

}