package main.java.events.ttps;

import main.java.database.graph.Edge;
import main.java.events.Prerequisite;
import main.java.provenanceGraph.ProvenanceGraph;

import java.util.List;

public abstract class TTP {
    private char severity;

    private List<Prerequisite> prerequisites;
    private String phase;

    /**
     * Prüft, ob das Geschehen an den Knoten einer Kante zu dem entsprechenden
     * TTP passt
     * @param edge Zu untersuchende Kante
     * @param graph Provenance-Graph
     * @return true, wenn TTP erkannt wird
     */
    public abstract boolean matches(Edge edge, ProvenanceGraph graph);


    /**
     * Prüft für alle Prärekonditionen des entsprechenden
     * TTPs, ob sie eingehalten werden
     * @param edge Die Kante dessen Knoten geprüft wird
     * @param graph Provenance-Graph
     * @return true, wenn alle Bedingungen eingehalten werden, sonst false
     */
    protected boolean prerequisitesMet(Edge edge, ProvenanceGraph graph ){
        for(Prerequisite p : prerequisites){
            if(!p.evaluate(edge, graph)){
                return false;
            }
        }
        return true;
    }

    public String getPhase(){
        return phase;
    }

    public char getSeverity(){
        return severity;
    }

    protected void setSeverity(char severity){
        this.severity = severity;
    }
    protected void setPhase(String phase){
        this.phase = phase;
    }

    protected void setPrerequisites(List<Prerequisite> prerequisites){
        this.prerequisites = prerequisites;
    }

    public abstract String getName();


}
