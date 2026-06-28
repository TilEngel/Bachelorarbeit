package main.java.events.ttps;

import main.java.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Liefert Config-Werte für TTPs
 * Vorlage von Claude.ai, anwendungsspezifische Details
 * selbst geschrieben
 */
public class TTPConfig {
    private static final Properties properties = new Properties();
    private static final Properties scoringProperties = new Properties();

    static {
        try(FileInputStream fis = new FileInputStream("ttp_config.properties")){
            properties.load(fis);
        }catch (IOException e){
            throw new RuntimeException("[ERR] ttp_config.properties nicht gefunden "+ e.getMessage());
        }
        try (FileInputStream fis = new FileInputStream("scoring.properties")){
            scoringProperties.load(fis);
        } catch (IOException e){
            throw new RuntimeException("[ERR] scoring.properties nicht gefunden: "+ e.getMessage());
        }
    }

    /**
     * Gibt Werte in config-Datei als Set zurück
     * @param prop Property, zu dem Werte geholt werden sollen
     * @return Menge an Werten
     */
    public static Set<String> getProperties(String prop){
        //schauen, ob Property überhaupt existiert
        String value = properties.getProperty(prop);
        if(value == null ||value.isBlank()){
            return Collections.emptySet();

        }
        //Werte holen
        Set<String> out = new HashSet<>();
        for(String s :value.split(",")){
            out.add(s.trim());
        }
        return out;
    }

    /**
     * Gibt Wert in config-Datei als int zurück
     * @param key Schlüssel, unter dem Wert in config-Datei liegt
     * @param fallback Rückgabe, falls key nicht existiert
     * @return Wert als int
     */
    public static int getIntProperty(String key, int fallback){
        String value = scoringProperties.getProperty(key);
        if(value == null || value.isBlank()){
            //Wenn key nicht gültig, Fallback verwenden
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());

        }catch (NumberFormatException e){
            Logger.logError("[ERR] Ungültiger Integer-Wert für "+ key+": "+ e.getMessage());
            return fallback;
        }
    }
}
