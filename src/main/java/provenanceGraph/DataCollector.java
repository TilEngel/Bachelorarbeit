package main.java.provenanceGraph;

import main.java.Logger;
import main.java.database.JDBCEngine;
import main.java.database.graph.*;

import java.util.List;
import java.util.Map;

/**
 * Agiert als Schnittstelle zur JDBCEngine.
 * Holt die richtigen Daten aus der Datenbank, verarbeitet sie zu Objekten
 * und gibt sie der Klasse ProvenanceGraph
 */
public class DataCollector {

    private static JDBCEngine engine;

    private final ProvenanceGraph graph = new ProvenanceGraph();


    public DataCollector(JDBCEngine engine){
        setEngine(engine);
    }

    /**
     * Holt alle nötigen Daten mit der JDBCEngine
     * aus der Datenbank und speichert sie im ProvenanceGraphen
     */
    public void collectData(){
        //Knoten
        collectSubjects();
        collectFiles();
        collectNetflows();
        //Kanten
        collectEvents();
    }

    /**
     * Erstellt Subjekt-Instanzen und legt sie in Graphen ab
     */
    private void collectSubjects(){
        int count=0;
        List<Map<String, Object>> rows = engine.getAllNodes('1');
        for(Map<String,Object> row : rows) {
            count++;
            String cmd = (String) row.get("cmd");
            String hashId = (String) row.get("hash_id");

            Subject s = new Subject(hashId,cmd);
            graph.addNode(s);
        }
        Logger.log("[INFO] "+ count+ " Subjects verarbeitet");

    }

    /**
     * Erstellt File-Instanzen und legt sie in Graphen ab
     */
    private void collectFiles(){
        List<Map<String, Object>> rows = engine.getAllNodes('2');
        int count = 0;
        for(Map<String,Object> row : rows) {
            count++;
            String path = (String) row.get("path");
            if(path == null){
                path = "[unknown]";
            }
            String hashId = (String) row.get("hash_id");

            File f = new File(hashId,path);
            graph.addNode(f);
        }
        Logger.log("[INFO] "+ count+ " Files verarbeitet");

    }

    /**
     * Erstellt Netflow-Instanzen und legt sie in Graphen ab
     */
    private void collectNetflows(){
        List<Map<String, Object>> rows = engine.getAllNodes('3');
        int count=0;
        for(Map<String,Object> row : rows) {

            count++;
            String srcAddr = (String) row.get("src_addr");
            String dstAddr = (String) row.get("dst_addr");
            String hashId = (String) row.get("hash_id");

            Netflow n = new Netflow(hashId, srcAddr,dstAddr);
            graph.addNode(n);
        }
        Logger.log("[INFO] "+ count + " Netflows verarbeitet");

    }

    /**
     * Erstellt Edge-Instanzen mit Verweisen auf beteiligte Knoten
     * legt diese in edges-Liste ab
     */
    private void collectEvents(){
        List<Map<String, Object>> rows = engine.getAllEvents();
        int count =0;

        for (Map<String,Object> row:rows ){
            count++;
            String srcId = (String) row.get("src_node");
            String dstId = (String) row.get("dst_node");
            //jeweilige Knoten-Instanzen aus NodeIndex holen
            Node srcNode = graph.getNode(srcId);
            Node dstNode = graph.getNode(dstId);

            // falls einer der Knoten nicht im Zeitfenster liegt
            if(srcNode == null || dstNode == null){
                continue;
            }
            String operation = (String) row.get("operation");
            String timestamp = String.valueOf(row.get("timestamp_rec"));

            Edge e = new Edge(srcNode,operation,dstNode,timestamp);

            graph.addEdge(e);
        }
        Logger.log("[INFO] "+ count + " Edges verarbeitet");
    }


    public void setEngine(JDBCEngine jdbcEngine){
        engine = jdbcEngine;
    }


    public ProvenanceGraph getGraph( ){
        return graph;
    }

}
