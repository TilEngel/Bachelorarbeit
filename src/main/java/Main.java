package main.java;

import main.java.database.JDBCEngine;
import main.java.events.ttps.*;
import main.java.provenanceGraph.DataCollector;

import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public class Main {
    public static final boolean INDIRECT_EDGES_BOTH_WAYS = false; //Zwei Knoten können sich gegenseitig mit indirekten Kanten referenzieren
    public static final boolean ROUND_THREAT_SCORES = true; //Bedrohungspunktzahl auf eine Nachkommastelle runden
    public static final int PF_THRESHOLD = 3; //Path-Factor Schwellenwert
    public static final int ALARM_THRESHOLD = 120; //Bedrohungspunktzahl, ab der Alarm gemeldet wird
    public static final int MENTION_SCENARIO_THRESHOLD = 10; //Szenarien unter diesen Wert, werden nicht erwähnt

    public static String TIMESTAMP_MIN = "";
    public static String TIMESTAMP_MAX = "";

    //Zu suchende TTP-Typen
    private static final List<TTP> initialCompromise1 = List.of(new Untrusted_Read());
    private static final List<TTP> initialCompromise2 = List.of(new Untrusted_File_Exec(), new Make_Mem_Exec());
    private static final List<TTP> establishFoothold = List.of(new Shell_Exec(), new CnC());
    private static final List<TTP> privilegeEscalation = List.of(new Switch_SU());
    private static final List<TTP> internalRecon = List.of(new Sensitive_Command());
    private static final List<TTP> cleanupTracks = List.of(new Clear_Logs(), new Sensitive_Temp_RM());
    public static final List<List<TTP>> PHASES = List.of(initialCompromise1, initialCompromise2,
            establishFoothold, privilegeEscalation, internalRecon, cleanupTracks);


    public static void main(String[] args) {
        Logger.doLogAll();

        setTimestamp();

        JDBCEngine jdbc = new JDBCEngine();
        DataCollector collector = new DataCollector(jdbc);
        try {
            jdbc.connect();

            collector.collectDataStreaming();

            jdbc.disconnect();

        } catch (SQLException e) {
            Logger.logError("Fehler in Main:" + e.getMessage());
        }

    }

    /*
     * setzt die Timestamps anhand eines Datums und einer Uhrzeit
     * (geschrieben von Claude.ai)
     */
    private static void setTimestamp() {
        ZonedDateTime startTime = ZonedDateTime.of(
                2018, 4, 6,
                11, 0, 0, 0,
                ZoneId.of("America/New_York")
        );
        ZonedDateTime endTime = ZonedDateTime.of(
                2018, 4, 6,
                12, 10, 0, 0,
                ZoneId.of("America/New_York")
        );

        TIMESTAMP_MIN = String.valueOf(startTime.toEpochSecond() * 1_000_000_000L);
        TIMESTAMP_MAX = String.valueOf(endTime.toEpochSecond() * 1_000_000_000L);
    }
}


