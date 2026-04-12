package main.java;

import main.java.database.graph.Node;
import main.java.events.ttps.TTP;

public class Logger {
    private static char doDebugMessages = '0';

    public static void doLogAll(){
        doDebugMessages = '0';
    }
    public static void doLogTTPMatches(){
        doDebugMessages = '1';
    }
    public static void doLogResultOnly(){
        doDebugMessages = '2';
    }

    public static void log(String message){
        if(doDebugMessages < '1'){
            System.out.println(message);
        }
    }

    public static void logTTPMatch(Node node, TTP ttp){
        if(doDebugMessages < '2') {
            System.out.println("[TTP] Match auf " + node.getName() + ": " + ttp.getName());
        }
    }

    public static void logResult(String message){
        if(doDebugMessages < '8'){
            System.out.println(message);
        }
    }
}
