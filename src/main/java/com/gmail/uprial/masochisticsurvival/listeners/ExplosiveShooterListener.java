package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.MasochisticSurvival;
import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.projectiles.ProjectileSource;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.MetadataHelper.*;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;

public class ExplosiveShooterListener implements Listener {
    private final MasochisticSurvival plugin;
    private final CustomLogger customLogger;
    private final double percentage;
    private final double power;

    public ExplosiveShooterListener(final MasochisticSurvival plugin,
                                    final CustomLogger customLogger,
                                    final double percentage,
                                    final double power) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.power = power;
    }

    private static final String MK_EXPLOSION = "rn_explosion";

    @SuppressWarnings({"unused"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (!event.isCancelled()) {
            final Projectile projectile = event.getEntity();
            final ProjectileSource shooter = projectile.getShooter();
            if ((projectile instanceof Arrow || projectile instanceof ThrownPotion)
                    && (shooter instanceof LivingEntity)
                    && !(shooter instanceof Player)) {

                final LivingEntity entity = (LivingEntity)shooter;

                final Boolean explosion = getMetadataOrDefault(plugin, entity, MK_EXPLOSION, ()  -> {
                    final boolean newExplosion;
                    if(RandomUtils.PASS(percentage)) {
                        newExplosion = true;
                        if (customLogger.isDebugMode()) {
                            customLogger.debug(String.format("%s of %s got explosion with power %.1f",
                                    projectile.getType(), format(entity), power));
                        }
                    } else {
                        newExplosion = false;
                    }

                    return newExplosion;
                });

                if((explosion) && (distance(projectile, entity) > power)) {
                    projectile.getWorld().createExplosion(projectile.getLocation(), (float)power, true);
                }
            }
        }
    }

    private double distance(final Entity entity1, final Entity entity2) {
        return entity1.getLocation().distance(entity2.getLocation());
    }

    public static ExplosiveShooterListener getFromConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);
        double power = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "power"), String.format("power of %s", title), 0.0D, 16.0D);

        if(percentage <= 0.0D){
            return null;
        }

        return new ExplosiveShooterListener(plugin, customLogger, percentage, power);
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, power: %s}",
                formatDoubleValue(percentage), formatDoubleValue(power));
    }
}
