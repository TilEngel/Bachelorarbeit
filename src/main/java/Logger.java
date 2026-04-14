package main.java;

import main.java.database.graph.Node;
import main.java.events.ttps.TTP;

public class Logger {
    private static char doDebugMessages = '0'; //0 = Alles, 1=Kein Debug, 2= Nur Ergebnisse

    /**
     * Logger gibt jede Nachricht aus
     */
    public static void doLogAll(){
        doDebugMessages = '0';
    }

    /**
     * Logger gibt nur Nachrichten mit höherer Priorität aus
     * (Keine einfachen Debug Nachrichten)(z.B. TTP-Matches)
     */
    public static void doLogPriority(){
        doDebugMessages = '1';
    }

    /**
     * Logger gibt nur Ergebnisse aus
     */
    public static void doLogResultOnly(){
        doDebugMessages = '2';
    }

    /**
     * Logger gibt unveränderte normale Nachricht aus
     * @param message Message
     */
    public static void log(String message){
        if(doDebugMessages < '1'){
            System.out.println(message);
        }
    }

    /**
     * Gibt TTP-Match aus
     * @param node Knoten mit Match
     * @param ttp gematchtes TTP
     */
    public static void logTTPMatch(Node node, TTP ttp){
        if(doDebugMessages < '2') {
            System.out.println("[TTP] Match auf " + node.getName() + ": " + ttp.getName());
        }
    }

    /**
     * Logger gibt Ergebnis unverändert aus
     * @param message Nachricht
     */
    public static void logResult(String message){
        if(doDebugMessages < '8'){
            System.out.println(message);
        }
    }
}
