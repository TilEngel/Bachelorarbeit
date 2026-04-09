package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.Netflow;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;
/**
 * CnC in Establish_Foothold
 * Unbekannte IP sendet zu Netflow
 * ip != trustedIP wird ersetzt durch hasInitialCompromiseAncestor()
 */

public class CnC extends TTP{

    public CnC(){
        setPhase("establish_foothold");
        setSeverity('H');
        setType(EventType.Type.EVENT_SENDTO);
        setPrerequisites(List.of(
                //Quellknoten ist Prozess
                (edge, graph) -> edge.getSrcNode() instanceof Subject,
                //Zielknoten ist Netflow
                (edge, graph)-> edge.getDstNode() instanceof Netflow
        ));
    }


    @Override
    public boolean matches(Edge edge, ProvenanceGraph graph){
        if(!edge.getOperation().equals(EventType.Type.EVENT_SENDTO.toString())){
            return false;
        }
        if(!prerequisitesMet(edge,graph)){
            return false;
        }

        return true;
    }

    @Override
    public String getName(){
        return "cnc";
    }

}
