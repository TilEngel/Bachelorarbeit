package main.java.database;

import main.java.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

import static main.java.Main.TIMESTAMP_MAX;
import static main.java.Main.TIMESTAMP_MIN;

/**
 * Engine, um Daten aus den Tabellen der Datenbank zu holen,
 * Daten müssen später zu Objekten verarbeitet werden
 */
public class JDBCEngine {
    //Zugangsdaten für die Datenbank
    private static final String DB_HOST;
    private static final String DB_PORT;
    private static final String DB_NAME;
    private static final String DB_USER;
    private static final String DB_PASSWD;

    //Holt private Daten aus properties Datei (Erstellt mit claude.ai)
    static {
        Properties props = new Properties();
        try(FileInputStream fis = new FileInputStream("config.properties")){
            props.load(fis);
        } catch(IOException e){
            throw new RuntimeException("[ERR] config.properties nicht gefunden: "+ e.getMessage());
        }
        DB_HOST = props.getProperty("DB_HOST");
        DB_PORT = props.getProperty("DB_PORT");
        DB_NAME = props.getProperty("DB_NAME");
        DB_USER = props.getProperty("DB_USER");
        DB_PASSWD = props.getProperty("DB_PASSWD");
    }

    private static final String JDBC_URL = "jdbc:postgresql://" + DB_HOST + ":"+ DB_PORT + "/"+ DB_NAME;

    private Connection connection;

    //private static final String TIMESTAMP_THRESH = "1522725000000000000"; // 675.000 Kanten

    /**
     * Stellt Verbindung zur Datenbank her
     * @throws SQLException falls Verbindung scheitert
     */
    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()){
            connection = DriverManager.getConnection(JDBC_URL, DB_USER, DB_PASSWD);
            Logger.log("[INFO] DB-Verbindung hergestellt: "+ JDBC_URL);
        } else {
            Logger.logPriority("[WARN] Es besteht bereits eine DB-Verbindung");
        }
    }

    /**
     * Schließt die Datenbankverbindung
     */
    public void disconnect() {
        if (connection != null){
            try {
                connection.close();
                Logger.log("[INFO] DB-Verbindung erfolgreich geschlossen");
            } catch (SQLException e) {
                Logger.logError("Fehler beim schließen der DB-Verbindung: "+e.getMessage() );
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }


    /**
     * Liefert alle Entitäten im Zeitraum (ohne Dopplung)
     * als Liste an Maps (Format [UUID, Objekt])
     * @param nodeType Tabelle aus der alle Knoten geliefert werden sollen (Wertebereich [1,3])
     * @return Liste aller relevanten Entitäten
     */
    public List<Map<String, Object>> getAllNodes(char nodeType) {
        //Richtige Tabelle wählen
        String table;
        if(nodeType =='1') {
            table = "subject_node_table";
        } else if(nodeType =='2'){
            table = "file_node_table";
        } else if(nodeType == '3') {
            table = "netflow_node_table";
        } else{
            Logger.log("[WARN] In ungültiger Tabelle nach Knoten gesucht");
            return null;
        }
        //Query: Alle Subjekte, die an Events vor TIMESTAMP_THRESH beteiligt sind
        String sql = "SELECT DISTINCT x.* " +
                "FROM " + table + " x " +
                "WHERE x.hash_id IN ( " +
                "SELECT src_node FROM event_table " +
                "WHERE timestamp_rec BETWEEN " + TIMESTAMP_MIN + " AND " + TIMESTAMP_MAX +
                " UNION " +
                "SELECT dst_node FROM event_table " +
                "WHERE timestamp_rec BETWEEN " + TIMESTAMP_MIN + " AND " + TIMESTAMP_MAX + ")";
        List<Map<String,Object>> rows = new ArrayList<>();

        try(Statement stmt = getConnection().createStatement()) {
            try (ResultSet rs = stmt.executeQuery(sql)) {
                ResultSetMetaData meta = rs.getMetaData();
                //Ergebnisse als Map speichern
                int columnCount = meta.getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= columnCount; i++) {
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    //Map zu Liste hinzufügen
                    rows.add(row);
                }
            } catch (SQLException e) {
                Logger.logError("getAllSubjectNodes: " + e.getMessage());
            }
        } catch (SQLException e) {
            Logger.logError("getAllSubjectNodes: " + e.getMessage());
        }
        Logger.log("[INFO] getAllNodes beendet. NodeType: "+nodeType);
        return rows;
    }

    /**
     * Liefert alle Kanten im Zeitraum
     * als Liste an Maps (Format [UUID, Object])
     * @return Liste aller Events
     */
    public List<Map<String,Object>> getAllEvents(){
        String sql= "SELECT * FROM event_table "+
                "WHERE timestamp_rec BETWEEN "+ TIMESTAMP_MIN+ " AND "+ TIMESTAMP_MAX+
                " ORDER BY timestamp_rec";
        List<Map<String,Object>> rows = new ArrayList<>();
        try(Statement stmt = getConnection().createStatement()){
            try(ResultSet rs = stmt.executeQuery(sql)){
                ResultSetMetaData meta = rs.getMetaData();
                int columnCount = meta.getColumnCount();
                while(rs.next()){
                    Map<String,Object> row = new LinkedHashMap<>();
                    for(int i=1; i<= columnCount; i++){
                        row.put(meta.getColumnName(i), rs.getObject(i));
                    }
                    rows.add(row);
                }
            }catch (SQLException e){
                Logger.logError("getAllEvents: "+ e.getMessage());
            }
        } catch (SQLException e){
            Logger.logError("getAllEvents(): "+e.getMessage());
        }
        return rows;
    }

}


