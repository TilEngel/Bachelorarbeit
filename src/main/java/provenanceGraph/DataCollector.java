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
            String uuid = (String) row.get("node_uuid");
            long nodeIndex = toLong(row.get("index_id"));
            String path = (String) row.get("path");
            String cmd = (String) row.get("cmd");
            String hashId = (String) row.get("hash_id");

            Subject s = new Subject(uuid, nodeIndex,hashId,path,cmd);
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
            String uuid = (String) row.get("node_uuid");
            long nodeIndex = toLong(row.get("index_id"));
            String path = (String) row.get("path");
            if(path == null){
                path = "[unknown]";
            }
            String hashId = (String) row.get("hash_id");

            File f = new File(uuid, nodeIndex,hashId,path);
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
            String uuid = (String) row.get("node_uuid");
            long nodeIndex = toLong(row.get("index_id"));
            String srcAddr = (String) row.get("src_addr");
            String srcPort = (String) row.get("src_port");
            String dstAddr = (String) row.get("dst_addr");
            String dstPort = (String)row.get("dst_port");
            String hashId = (String) row.get("hash_id");

            Netflow n = new Netflow(uuid, nodeIndex, hashId, srcAddr,srcPort,dstAddr,dstPort);
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
            //jwlg. Knoten-Instanzen aus NodeIndex holen
            Node srcNode = graph.getNode(srcId);
            Node dstNode = graph.getNode(dstId);

            // falls einer der Knoten nicht im Zeitfenster liegt
            if(srcNode == null || dstNode == null){
                continue;
            }
            String eventUuid = (String) row.get("event_uuid");
            String operation = (String) row.get("operation");
            String timestamp = String.valueOf(row.get("timestamp_rec"));
            long id = toLong(row.get("_id"));

            Edge e = new Edge(srcNode,operation,dstNode,eventUuid,timestamp,id);

            graph.addEdge(e);
        }
        Logger.log("[INFO] "+ count + " Edges verarbeitet");
    }


    public void setEngine(JDBCEngine jdbcEngine){
        engine = jdbcEngine;
    }

    /*
    Hilfsmethode, um Object zu long zu konvertieren
     */
    private long toLong(Object obj){
        if(obj instanceof Long) return (Long) obj;
        if(obj instanceof Integer) return ((Integer)obj).longValue();
        if(obj instanceof String) return Long.parseLong((String)obj);
        throw new IllegalArgumentException("[ERR] toLong nicht möglich: "+obj);
    }

    public ProvenanceGraph getGraph( ){
        return graph;
    }

}
