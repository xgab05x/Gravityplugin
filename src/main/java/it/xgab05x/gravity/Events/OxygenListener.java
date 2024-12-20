package it.xgab05x.gravity.Events;

import it.xgab05x.gravity.Gravity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;

public class OxygenListener implements Listener {
    private static final Map<Player, Float> oxygenLevels = new HashMap<>();
    private static final float MAX_OXYGEN = (float) Gravity.getInstance().getConfig().getDouble("oxygen.max-oxygen", 300);
    private static final int TICKS_TO_DAMAGE = 20;


    public static void startOxygenTask(Gravity plugin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission("gravity.oxygen")) {
                        continue;
                    }
                    oxygenLevels.putIfAbsent(player, MAX_OXYGEN);
                    float currentOxygen = oxygenLevels.get(player);
                    boolean wearingHelmet = isWearingSpaceHelmet(player);
                    if (!wearingHelmet) {
                        currentOxygen -= (float) Gravity.getInstance().getConfig().getDouble("oxygen.depletion-rate", 0.5);
                    } else {
                        currentOxygen += (float) Gravity.getInstance().getConfig().getDouble("oxygen.helmet-boost", 6);
                        if (currentOxygen > MAX_OXYGEN) {
                            currentOxygen = MAX_OXYGEN;
                        }
                    }
                    if (currentOxygen <= 0) {
                        if (player.getHealth() > 0) {
                            if (player.getTicksLived() % TICKS_TO_DAMAGE == 0) {
                                player.damage(1.0);
                            }
                        }
                    }
                    oxygenLevels.put(player, currentOxygen);
                    updateOxygenBar(player, currentOxygen);
                }
            }
        }.runTaskTimer(plugin, 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (!isWearingSpaceHelmet(player) && !player.hasPermission("gravity.oxygen")) WarningNoOssigeno(player);
                }
            }
        }.runTaskTimer(plugin, 0, Gravity.getInstance().getConfig().getInt("oxygen.warning-interval", 200));
    }

    private static boolean isWearingSpaceHelmet(Player player) {
        ItemStack helmet = player.getInventory().getHelmet();
        if (helmet == null || helmet.getType() != Material.PLAYER_HEAD) {
            return false;
        }
        ItemMeta meta = helmet.getItemMeta();
        if (!(meta instanceof SkullMeta)) {
            return false;
        }
        SkullMeta skullMeta = (SkullMeta) meta;
        String helmetName = skullMeta.getDisplayName();
        String configName = Gravity.getInstance().getConfig().getString("spacehelmet.name", "");
        if (!helmetName.equalsIgnoreCase(configName)) {
            return false;
        }
        String configTextureUrl = Gravity.getInstance().getConfig().getString("spacehelmet.texture", "");
        if (configTextureUrl.isEmpty()) {
            return false;
        }
        PlayerProfile profile = skullMeta.getOwnerProfile();
        if (profile == null) {
            return false;
        }
        String helmetTexture = profile.getTextures().getSkin().toString();
        return helmetTexture.equals(configTextureUrl);
    }

    private static void WarningNoOssigeno(Player player) {
        player.sendTitle("§c§lATTENZIONE!", "§7Fonte di ossigeno interrotta", 10, 80, 10);
        player.sendMessage("\n§c§lATTENZIONE!\n\n§7Fonte di ossigeno interrotta!\n§7Indossa la maschera o controlla le tue bombole\n§7oppure recati in un'area climatizzata!\n\n§cRimanere troppo tempo senza ossigeno è pericoloso!\n");
    }

    private static void updateOxygenBar(Player player, float currentOxygen) {
        int remainingAir = (int) ((currentOxygen / MAX_OXYGEN) * 300);

        if (currentOxygen <= 0) {
            player.setRemainingAir(0);
        } else {
            player.setRemainingAir(remainingAir);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        oxygenLevels.put(player, (float) MAX_OXYGEN);
    }
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        oxygenLevels.remove(player);
    }
}
