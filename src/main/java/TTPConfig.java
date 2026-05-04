package main.java;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class TTPConfig {
    private static final Properties properties = new Properties();

    static {
        try(FileInputStream fis = new FileInputStream("ttp_config.properties")){
            properties.load(fis);
        }catch (IOException e){
            throw new RuntimeException("[ERR] ttp_config.properties nicht gefunden "+ e.getMessage());
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
        for(String s : value.split(",")){
            out.add(s.trim());
        }
        return out;
    }
}
