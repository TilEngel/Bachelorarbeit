package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;
import java.util.Set;

/**
 * Switch_SU in Privilege_Escalation
 * Nach Initial_Compromise werden superuser-tools verwendet
 */
public class Switch_SU extends TTP{
    private static final Set<String> SUPERUSER_TOOLS = Set.of(
            "sudo", "su", "doas");

    public Switch_SU(){
        setPhase("privilege_escalation");
        setSeverity('H');
        setPrerequisites(List.of(
                //Zielknoten muss Prozess sein
                (edge, graph) -> edge.getDstNode() instanceof Subject,
                //Prozessname ist Priv-Escalation-Tool
                ((edge, graph) -> {
                    if(!(edge.getDstNode() instanceof Subject)) return false;
                    Subject s =(Subject) edge.getDstNode();
                    return SUPERUSER_TOOLS.contains(s.getCmd());
                })
        ));
    }

    @Override
    public boolean matches(Edge edge, ProvenanceGraph graph){
        if(!edge.getOperation().equals(EventType.Type.EVENT_CHANGE_PRINCIPAL.toString())){
            return false;
        }
        if(!prerequisitesMet(edge, graph)){
            return false;
        }

        return true;
    }
    @Override
    public String getName(){
        return "switch_su";
    }

}
