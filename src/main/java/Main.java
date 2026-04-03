package main.java;

import main.java.database.JDBCEngine;
import main.java.provenanceGraph.DataCollector;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args){
        JDBCEngine jdbc = new JDBCEngine();
        DataCollector collector = new DataCollector(jdbc);
        try {
            jdbc.connect();

            collector.collectData();

            jdbc.disconnect();

        }catch (SQLException e){
            System.err.println("[ERR] Fehler in Main:" +e.getMessage());
        }
        collector.printEdges();
    }
}
