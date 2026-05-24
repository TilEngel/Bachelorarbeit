package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.database.graph.Netflow;
import main.java.database.graph.Subject;
import main.java.events.EventType;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;
import java.util.Set;

/**
 * Untrusted_Read in Initial_Compromise
 * unbekannte IP liest
 * Ausgangspunkt für alle weiteren TTPs.
 * Es kann nicht zuverlässig gesagt werden, welche IPs vertrauenswürdig sind
 * und welche nicht :(
 */
public class Untrusted_Read extends TTP {

    private static final Set<String> TRUSTED_IPS = TTPConfig.getProperties("untrusted_read.trusted_ips");

    public Untrusted_Read(){
        setPhase("initial_compromise");
        setSeverity('L');
        setPrerequisites(List.of(
                //Quellknoten muss untrusted IP haben
                (edge) -> {
                    if(!(edge.getSrcNode() instanceof Netflow)) return false;
                    if(!(edge.getDstNode() instanceof Subject)) return false;
                    Netflow n = (Netflow) edge.getSrcNode();
                    return !TRUSTED_IPS.contains(n.getDstAddr());
                }
        ));
    }

    @Override
    public boolean matches(Edge edge){
        if(!edge.getOperation().equals(EventType.Type.EVENT_RECVFROM.toString())){
            return false;
        }
        return prerequisitesMet(edge);
    }

    @Override
    public String getName(){
        return "untrusted_read";
    }

}

