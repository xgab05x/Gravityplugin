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
    public String prefix = "§c[§7Gravity§c]";

    @Override
    public void onEnable() {
        if (instance != null) {
            getLogger().warning(prefix + "§7Il plugin è già stato inizializzato.");
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
            getLogger().warning(prefix + "§7Errore durante l'inizializzazione di FileManager: " + e.getMessage());
        }
        getServer().getPluginManager().registerEvents(new EventsListener(), this);
        getServer().getPluginManager().registerEvents(new ProjectileListener(), this);
        getServer().getPluginManager().registerEvents(new OxygenListener(), this);

        OxygenListener.startOxygenTask(this);
        EventsListener.applyGravityToExistingEntities();
        EventsListener.startPeriodicGravityCheck();
        EventsListener.startFallingBlockTask();

        this.getCommand("gravity").setExecutor(new GravityCommands());
        Bukkit.getConsoleSender().sendMessage(prefix + " §7Plugin attivato con successo!");
        Bukkit.getConsoleSender().sendMessage("§7Gravity plugin by @Poliformica (xgab05x)");
        Bukkit.getConsoleSender().sendMessage("§7Versione: §c" + getVersion());

    }

    @Override
    public void onDisable() {
        try {
            FileManager.saveExcludedPlayers();
        } catch (Exception e) {
            getLogger().warning(prefix + " §7Errore durante il salvataggio dei giocatori esclusi: " + e.getMessage());
        }
        Bukkit.getConsoleSender().sendMessage(prefix + " §7Plugin disattivato con successo!");
    }

    public String getVersion() {
        return "Alpha 1.4.0";
    }
    public String getPrefix() {
        return prefix;
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
