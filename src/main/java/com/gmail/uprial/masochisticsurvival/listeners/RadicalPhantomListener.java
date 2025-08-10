package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.MasochisticSurvival;
import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.google.common.collect.ImmutableMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Map;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.MetadataHelper.*;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;
import static com.gmail.uprial.masochisticsurvival.common.Utils.seconds2ticks;

public class RadicalPhantomListener implements Listener {
    private final MasochisticSurvival plugin;
    private final CustomLogger customLogger;
    private final double percentage;
    private final double power;
    private final int cooldown;
    private final boolean infoLogAboutActions;

    public RadicalPhantomListener(final MasochisticSurvival plugin,
                                  final CustomLogger customLogger,
                                  final double percentage,
                                  final double power,
                                  final int cooldown,
                                  final boolean infoLogAboutActions) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.power = power;
        this.cooldown = cooldown;
        this.infoLogAboutActions = infoLogAboutActions;
    }

    // WARNING: please keep the legacy prefix for backward compatibility
    private static final String MK_EXPLODED = "rn_exploded";

    @SuppressWarnings({"unused"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!event.isCancelled() && (RandomUtils.PASS(percentage))) {

            if(event.getEntity() instanceof Phantom) {

                explode((Phantom)event.getEntity(),
                        String.format(" damaged by %s", format(event.getDamager())));

            } else if (event.getDamager() instanceof Phantom) {

                explode((Phantom)event.getDamager(),
                        String.format(" damaged %s", format(event.getEntity())));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDeath(EntityDeathEvent event) {
        if ((event.getEntity() instanceof Phantom) && (RandomUtils.PASS(percentage))) {

            explode((Phantom)event.getEntity(), "");
        }
    }

    private static final Map<PotionEffectType,Integer> COOLDOWN_EFFECTS = ImmutableMap.<PotionEffectType,Integer>builder()
            .put(PotionEffectType.GLOWING, 0)
            .put(PotionEffectType.SPEED, 1)
            // The highest potion amplifier that protects less than for 100%
            .put(PotionEffectType.RESISTANCE, 3)
            .build();

    private void explode(final Phantom phantom, final String context) {
        final Long currTime = System.currentTimeMillis();
        final Long lastTime = getMetadata(phantom, MK_EXPLODED);

        if((lastTime == null) || (currTime - lastTime > 1_000L * cooldown)) {
            setMetadata(plugin, phantom, MK_EXPLODED, currTime);

            // Must be AFTER setMetadata() to prevent infinite cycles
            phantom.getWorld().createExplosion(
                    phantom.getLocation(), (float) power, true, true, phantom);

            // If not from onEntityDeath()
            if(!context.isEmpty()) {
                for(Map.Entry<PotionEffectType,Integer> entry : COOLDOWN_EFFECTS.entrySet()) {
                    phantom.addPotionEffect(
                            new PotionEffect(entry.getKey(), seconds2ticks(cooldown), entry.getValue()));
                }
            }

            final String message = String.format("%s%s exploded with power %.1f",
                    format(phantom), context, power);

            if(infoLogAboutActions) {
                customLogger.info(message);
            } else {
                customLogger.debug(message);
            }
        }
    }

    public static RadicalPhantomListener getFromConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if(percentage <= 0.0D){
            return null;
        }

        double power = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "power"), String.format("power of %s", title), 0.0D, 16.0D);

        int cooldown = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "cooldown"), String.format("cooldown of %s", title), 1, 300);

        boolean infoLogAboutActions = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "info-log-about-actions"), String.format("'info-log-about-actions' flag of %s", title));

        return new RadicalPhantomListener(plugin, customLogger, percentage, power, cooldown, infoLogAboutActions);
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, power: %s, cooldown: %d, info-log-about-actions: %b}",
                formatDoubleValue(percentage), formatDoubleValue(power), cooldown, infoLogAboutActions);
    }
}
