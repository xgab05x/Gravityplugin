package it.xgab05x.gravity;

import it.xgab05x.gravity.Commands.GravityCommands;
import it.xgab05x.gravity.Events.ProjectileListener;
import it.xgab05x.gravity.utils.FileManager;
import it.xgab05x.gravity.Events.EventsListener;
import it.xgab05x.gravity.Events.OxygenListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class Gravity extends JavaPlugin {
    private static Gravity instance;
    private List<String> excludedPlayers;

    @Override
    public void onEnable() {
        if (instance != null) {
            getLogger().warning("Il plugin è già stato inizializzato.");
            return;
        }
        instance = this;
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        saveDefaultConfig();
        try {
            FileManager.setup();
            FileManager.reloadExcludedPlayers();
        } catch (Exception e) {
            getLogger().warning("Errore durante l'inizializzazione di FileManager: " + e.getMessage());
        }
        getServer().getPluginManager().registerEvents(new EventsListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileListener(), this);
        getServer().getPluginManager().registerEvents(new OxygenListener(), this);

        OxygenListener.startOxygenTask(this);
        EventsListener.applyGravityToExistingEntities();
        EventsListener.startPeriodicGravityCheck();
        EventsListener.startFallingBlockTask();

        this.getCommand("gravity").setExecutor(new GravityCommands());
        Bukkit.getConsoleSender().sendMessage("§7[Gravity] Plugin attivato con successo!");
        Bukkit.getConsoleSender().sendMessage("§7Versione: " + getVersion());

    }

    @Override
    public void onDisable() {
        try {
            FileManager.saveExcludedPlayers();
        } catch (Exception e) {
            getLogger().warning("Errore durante il salvataggio dei giocatori esclusi: " + e.getMessage());
        }
        Bukkit.getConsoleSender().sendMessage("§7[Gravity] Plugin disattivato con successo!");
    }

    public String getVersion() {
        return "Alpha 1.0.0";
    }

    public static Gravity getInstance() {
        return instance;
    }

    public List<String> getExcludedPlayers() {
        return excludedPlayers;
    }

    public void setExcludedPlayers(List<String> excludedPlayers) {
        this.excludedPlayers = excludedPlayers;
    }
}
