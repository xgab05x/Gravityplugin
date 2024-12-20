package it.xgab05x.gravity.Events;

import it.xgab05x.gravity.Gravity;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.util.Vector;

public class ProjectileListener implements Listener {

    private static final double GENERAL_SPEED_MULTIPLIER = 1.7;
    private static final double FIREWORK_SPEED_MULTIPLIER = 2;
    private static final double FIREWORK_VERTICAL_MULTIPLIER = 2.0;
    private static final int UPDATE_INTERVAL = 5;

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof Firework firework) {
            handleFireworkLaunch(firework);
        } else {
            applyGeneralProjectileGravity(projectile);
        }
    }

    private void handleFireworkLaunch(Firework firework) {
        Vector velocity = firework.getVelocity();
        velocity.multiply(FIREWORK_SPEED_MULTIPLIER);
        velocity.setY(velocity.getY() * FIREWORK_VERTICAL_MULTIPLIER);
        firework.setVelocity(velocity);
        applyAlteredGravity(firework);
    }

    private void applyGeneralProjectileGravity(Projectile projectile) {
        Vector velocity = projectile.getVelocity();
        velocity.multiply(GENERAL_SPEED_MULTIPLIER);
        projectile.setVelocity(velocity);
        applyAlteredGravity(projectile);
    }

    private void applyAlteredGravity(Projectile projectile) {
        new org.bukkit.scheduler.BukkitRunnable() {
            private static final double VERTICAL_DECAY_DOWN = 0.83;

            @Override
            public void run() {
                if (!projectile.isValid() || projectile.isDead()) {
                    this.cancel();
                    return;
                }
                Vector velocity = projectile.getVelocity();
                if (velocity.getY() < 0) {
                    velocity.setY(velocity.getY() * VERTICAL_DECAY_DOWN);
                }
                projectile.setVelocity(velocity);
            }
        }.runTaskTimer(Gravity.getInstance(), 0, UPDATE_INTERVAL);
    }
}

