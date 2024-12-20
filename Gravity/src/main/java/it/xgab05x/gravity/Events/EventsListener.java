package it.xgab05x.gravity.Events;

import it.xgab05x.gravity.Gravity;
import it.xgab05x.gravity.utils.FileManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;

public class EventsListener implements Listener {

    private static final Set<Entity> gravityEntities = new HashSet<>();
    private static final int GRAVITY_UPDATE_INTERVAL = 100;

    private static final List<FallingBlock> trackedBlocks = new ArrayList<>();

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        FileManager.reloadExcludedPlayers();
        Player player = event.getPlayer();
        applyGravity(player);
        checkGravity(player);
    }

    public static void applyGravity(Entity entity) {
        if (entity instanceof Player player) {
            if (Gravity.getInstance().getExcludedPlayers().contains(player.getName())) {
                return;
            }
        }

        if (entity instanceof LivingEntity livingEntity) {
            if (!(entity instanceof Creeper creeper && creeper.getIgniter() != null)) {
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, Integer.MAX_VALUE, 2, false, false, false));
                livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, Integer.MAX_VALUE, 10, false, false, false));
            }
        }
    }

    public static void removeGravity(Player player) {
        if (player == null) return;
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
        player.removePotionEffect(PotionEffectType.SLOW_FALLING);
    }

    @EventHandler
    public void onItemSpawn(ItemSpawnEvent event) {
        Vector initialPush = new Vector(0, 0.25, 0);
        event.getEntity().setVelocity(event.getEntity().getVelocity().add(initialPush));
        applyGravityToItem(event.getEntity());
    }

    @EventHandler
    public void onCreeperExplosion(ExplosionPrimeEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof Creeper creeper) {
                creeper.removePotionEffect(PotionEffectType.JUMP_BOOST);
                creeper.removePotionEffect(PotionEffectType.SLOW_FALLING);
        }
    }

    public static void applyGravityToItem(Entity item) {
        final double verticalDecayUp = 0.7;
        final double verticalDecayDown = 0.7;
        final double stopThreshold = 0.01;
        final int updateInterval = 3;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!item.isDead() && item.getLocation().getY() > 0) {
                    Vector velocity = item.getVelocity();
                    double verticalSpeed = velocity.getY();
                    verticalSpeed *= verticalSpeed > 0 ? verticalDecayUp : verticalDecayDown;
                    if (Math.abs(verticalSpeed) < stopThreshold && item.isOnGround()) {
                        this.cancel();
                        return;
                    }
                    velocity.setY(verticalSpeed);
                    item.setVelocity(velocity);
                } else {
                    this.cancel();
                }
            }
        }.runTaskTimer(Gravity.getInstance(), 0, updateInterval);
    }

    public static void applyGravityToExistingEntities() {
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof LivingEntity) {
                    applyGravity(entity);
                }
            }
        }
    }

    public static void startPeriodicGravityCheck() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (entity instanceof LivingEntity && !gravityEntities.contains(entity)) {
                            applyGravity(entity);
                        }
                    }
                }
            }
        }.runTaskTimer(Gravity.getInstance(), 0L, GRAVITY_UPDATE_INTERVAL);
    }

    private void checkGravity(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (player.isOnline()) {
                    applyGravity(player);
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(Gravity.getInstance(), 0L, GRAVITY_UPDATE_INTERVAL);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setDamage(event.getDamage() * 0.05);
            }
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            Material blockMaterial = fallingBlock.getBlockData().getMaterial();
            if (blockMaterial == Material.SAND || blockMaterial == Material.GRAVEL || blockMaterial == Material.ANVIL) {
                trackedBlocks.add(fallingBlock);
                Vector velocity = fallingBlock.getVelocity();
                velocity.setY(velocity.getY() * 0.2);
                fallingBlock.setVelocity(velocity);
            }
        }
    }

    @EventHandler
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock fallingBlock) {
            trackedBlocks.remove(fallingBlock);
        }
    }

    public static void startFallingBlockTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                Iterator<FallingBlock> iterator = trackedBlocks.iterator();
                while (iterator.hasNext()) {
                    FallingBlock fallingBlock = iterator.next();
                    if (fallingBlock.isDead() || fallingBlock.isOnGround()) {
                        iterator.remove();
                        continue;
                    }
                    Vector velocity = fallingBlock.getVelocity();
                    if (velocity.getY() < 0) {
                        velocity.setY(velocity.getY() * 0.6);
                        fallingBlock.setVelocity(velocity);
                    }
                }
            }
        }.runTaskTimer(Gravity.getInstance(), 0, 5);
    }
}