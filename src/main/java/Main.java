package main.java;

import main.java.database.JDBCEngine;

import java.sql.SQLException;

public class Main {

    public static void main(String[] args){
        JDBCEngine jdbc = new JDBCEngine();
        try {
            jdbc.connect();

            jdbc.getAllNodes('1');
            jdbc.getAllEventsTest();

            jdbc.disconnect();

        }catch (SQLException e){
            System.err.println("[ERR] Fehler in Main:" +e.getMessage());
        }
    }
}
