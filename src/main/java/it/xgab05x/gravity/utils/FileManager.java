package it.xgab05x.gravity.utils;

import it.xgab05x.gravity.Gravity;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FileManager {

    private static File excludedFile;
    private static FileConfiguration excludedConfig;
    public static void setup() {
        File dataFolder = Gravity.getInstance().getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
            Gravity.getInstance().getLogger().info("Cartella di dati creata.");
        }
        excludedFile = new File(dataFolder, "excluded.yml");
        if (!excludedFile.exists()) {
            try {
                excludedFile.createNewFile();
                Gravity.getInstance().getLogger().info("File 'excluded.yml' creato con successo.");
            } catch (IOException e) {
                Gravity.getInstance().getLogger().severe("Errore durante la creazione del file 'excluded.yml': " + e.getMessage());
                e.printStackTrace();
            }
        }
        excludedConfig = YamlConfiguration.loadConfiguration(excludedFile);
    }

    public static void saveExcludedPlayers() {
        List<String> excludedPlayers = Gravity.getInstance().getExcludedPlayers();
        excludedConfig.set("excludedPlayers", excludedPlayers);
        try {
            excludedConfig.save(excludedFile);
            Gravity.getInstance().getLogger().info("Giocatori esclusi salvati con successo.");
        } catch (IOException e) {
            Gravity.getInstance().getLogger().severe("Errore durante il salvataggio dei giocatori esclusi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void reloadExcludedPlayers() {
        try {
            excludedConfig = YamlConfiguration.loadConfiguration(excludedFile);
            List<String> excludedPlayers = excludedConfig.getStringList("excludedPlayers");
            Gravity.getInstance().setExcludedPlayers(excludedPlayers);
            Gravity.getInstance().getLogger().info("Giocatori esclusi ricaricati con successo.");
        } catch (Exception e) {
            Gravity.getInstance().getLogger().severe("Errore durante il caricamento dei giocatori esclusi: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static FileConfiguration getExcludedConfig() {
        return excludedConfig;
    }
    public static File getExcludedFile() {
        return excludedFile;
    }
}
