package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.Set;
import java.util.List;

/**
 * Sensitive_Command in Internal_Recon
 * Nach Initial_Compromise wird ein potentiell gefährlicher Command verwendet
 */
public class Sensitive_Command extends TTP {
    private static final Set<String> SENSITIVE_COMMANDS = Set.of(
            "whoami", "hostname", "ifconfig", "netstat", "uname",
            //nicht explizit genannt aber auch sinnvoll
            "id", "ps", "w", "who", "last", "find", "locate"
    );

    public Sensitive_Command(){
        setPhase("internal_recon");
        setSeverity('H');
        setPrerequisites(List.of(
                //Quellknoten muss Prozess sein
                (edge,graph) -> edge.getSrcNode() instanceof Subject,
                //Zielknoten muss Prozess sein
                (edge, graph) -> edge.getDstNode() instanceof Subject,
                //Zielknoten muss sensitiver Befehl sein
                (edge, graph) -> {
                    if(!(edge.getDstNode() instanceof Subject)) return  false;
                    Subject s = (Subject) edge.getDstNode();
                    return SENSITIVE_COMMANDS.contains(s.getCmd());
                }
        ));

    }


    @Override
    public boolean matches(Edge edge, ProvenanceGraph graph){
        if(!edge.getOperation().equals(EventType.Type.EVENT_FORK.toString())){
            return false;
        }
        if(!prerequisitesMet(edge,graph)){
            return false;
        }

        return true;
    }

    @Override
    public String getName(){
        return "sensitive_command";
    }


}

