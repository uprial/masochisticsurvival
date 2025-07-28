package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;

public class HydraSpiderListener implements Listener {
    private final CustomLogger customLogger;
    private final double percentage;
    private final int amount;
    private final double scale;
    private final double speed;
    private final double health;

    public HydraSpiderListener(final CustomLogger customLogger,
                               final double percentage,
                               final int amount,
                               final double scale,
                               final double speed,
                               final double health) {
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.amount = amount;
        this.scale = scale;
        this.speed = speed;
        this.health = health;
    }

    @SuppressWarnings({"unused"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if((event.getEntity() instanceof Spider)
                && (RandomUtils.PASS(percentage))) {

            final Spider spider = (Spider)event.getEntity();

            final LivingEntity target;

            if(spider.getTarget() != null) {
                target = spider.getTarget();
            } else if ((event.getDamageSource().getCausingEntity() != null)
                    && (event.getDamageSource().getCausingEntity() instanceof LivingEntity)) {
                target = (LivingEntity) event.getDamageSource().getCausingEntity();
            /*
                Not to be confused with getCausingEntity(),
                the direct entity is the entity that actually inflicted the damage.

                If, for example, the receiver was damaged by a projectile,
                the projectile would be returned.
             */
            } else if ((event.getDamageSource().getDirectEntity() != null)
                    && (event.getDamageSource().getDirectEntity() instanceof LivingEntity)) {
                target = (LivingEntity)event.getDamageSource().getDirectEntity();
            } else {
                return;
            }

            final double newScale = spider.getAttribute(Attribute.SCALE).getBaseValue() * scale;
            final double newSpeed = spider.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue() * speed;
            final double newHealth = spider.getAttribute(Attribute.MAX_HEALTH).getBaseValue() * health;

            for(int i = 0; i < amount; i++) {
                final Spider hydra
                        = (Spider)spider.getWorld().spawnEntity(spider.getLocation(), spider.getType());

                hydra.getAttribute(Attribute.SCALE).setBaseValue(newScale);
                hydra.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(newSpeed);
                hydra.getAttribute(Attribute.MAX_HEALTH).setBaseValue(newHealth);
                if(health > 1.0D) {
                    /*
                        Because of rounding of float point variables we need to make sure that
                        health of entity is lower than its max. health.
                        So, we reduce an entity's health by this value.
                     */
                    hydra.setHealth(newHealth - 0.000001D);
                }
                hydra.setTarget(target);
            }

            customLogger.info(String.format("%s multiplied %d times with %.2f scale, %.2f speed, %.2f health and %s target",
                    format(spider), amount, newScale, newSpeed, newHealth, format(target)));
        }
    }

    public static HydraSpiderListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if(percentage <= 0.0D){
            return null;
        }

        int amount = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "amount"), String.format("amount of %s", title), 0, 10);

        if(amount <= 0){
            return null;
        }

        double scale = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "scale"), String.format("scale of %s", title), 0.0D, 2.0D);
        double speed = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "speed"), String.format("speed of %s", title), 0.0D, 2.0D);
        double health = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "health"), String.format("health of %s", title), 0.0D, 2.0D);

        return new HydraSpiderListener(customLogger, percentage, amount, scale, speed, health);
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, amount: %d, " +
                        "scale: %s, speed: %s, health: %s}",
                formatDoubleValue(percentage), amount,
                formatDoubleValue(scale), formatDoubleValue(speed), formatDoubleValue(health));
    }
}
