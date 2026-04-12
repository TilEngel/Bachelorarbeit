package main.java;

import main.java.database.graph.Node;
import main.java.events.ttps.TTP;

public class Logger {
    private static boolean doDebugMessages = true;

    public static void doDebug(boolean bool){
        doDebugMessages= bool;
    }

    public static void log(String message){
        if(doDebugMessages){
            System.out.println(message);
        }
    }

    public static void logTTPMatch(Node node, TTP ttp){
        System.out.println("[TTP] Match auf "+ node.getName() + ": "+ ttp.getName());
    }
}
