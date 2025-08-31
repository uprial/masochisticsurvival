package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.MasochisticSurvival;
import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.D2C;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.google.common.collect.ImmutableMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.MetadataHelper.*;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;
import static com.gmail.uprial.masochisticsurvival.common.Utils.seconds2ticks;

public class NastyArcherListener implements Listener {
    private final MasochisticSurvival plugin;
    private final CustomLogger customLogger;
    private final double positivePercentage;
    private final double negativePercentage;
    private final int negativePercentageD2CM;

    public NastyArcherListener(final MasochisticSurvival plugin,
                               final CustomLogger customLogger,
                               final double positivePercentage,
                               final double negativePercentage,
                               final int negativePercentageD2CM) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.positivePercentage = positivePercentage;
        this.negativePercentage = negativePercentage;
        this.negativePercentageD2CM = negativePercentageD2CM;
    }

    // WARNING: please keep the legacy prefix for backward compatibility
    private static final String MK_EFFECTS = "rn_effects";

    private enum E {
        USELESS(true, 30, 2),
        GOOD(true, 15, 1),
        AMAZING(true, 5, 0),

        INCONVENIENT(false, 60, 2),
        PAINFUL(false, 30, 1),
        INSANE(false, 1, 0);

        private final boolean positive;
        private final int duration;
        private final int amplifier;

        E(final boolean positive, final int duration, final int amplifier) {
            this.positive = positive;
            this.duration = duration;
            this.amplifier = amplifier;
        }

        public boolean isPositive() {
            return positive;
        }

        public int getAmplifier() {
            return amplifier;
        }

        public int getDuration() {
            return duration;
        }
    }
    /*
        Effect type options: effect -> E

        Source: MineshaftPopulator.chestLootTable.POTION
     */
    final private Map<PotionEffectType,E> effectMap = ImmutableMap .<PotionEffectType, E>builder()
            .put(PotionEffectType.ABSORPTION, E.GOOD)
            .put(PotionEffectType.BAD_OMEN, E.INCONVENIENT) // negative
            .put(PotionEffectType.BLINDNESS, E.PAINFUL) // negative
            // CONDUIT_POWER: duplicates WATER_BREATHING
            .put(PotionEffectType.DARKNESS, E.INCONVENIENT) // negative
            // DOLPHINS_GRACE: doesn't work
            .put(PotionEffectType.FIRE_RESISTANCE, E.USELESS)
            .put(PotionEffectType.GLOWING, E.USELESS)
            .put(PotionEffectType.HASTE, E.USELESS)
            .put(PotionEffectType.HEALTH_BOOST, E.AMAZING)
            .put(PotionEffectType.HERO_OF_THE_VILLAGE, E.USELESS)
            .put(PotionEffectType.HUNGER, E.PAINFUL) // negative
            .put(PotionEffectType.INFESTED, E.INCONVENIENT) // negative
            // 8(♥) per second
            .put(PotionEffectType.INSTANT_DAMAGE, E.INSANE) // negative
            .put(PotionEffectType.INSTANT_HEALTH, E.GOOD)
            .put(PotionEffectType.INVISIBILITY, E.GOOD)
            .put(PotionEffectType.JUMP_BOOST, E.GOOD)
            .put(PotionEffectType.LEVITATION, E.PAINFUL) // negative
            .put(PotionEffectType.LUCK, E.USELESS)
            .put(PotionEffectType.MINING_FATIGUE, E.INCONVENIENT) // negative
            .put(PotionEffectType.NAUSEA, E.INCONVENIENT) // negative
            .put(PotionEffectType.NIGHT_VISION, E.USELESS)
            .put(PotionEffectType.OOZING, E.INCONVENIENT) // negative
            .put(PotionEffectType.POISON, E.PAINFUL) // negative
            // RAID_OMEN: duplicates BAD_OMEN
            .put(PotionEffectType.REGENERATION, E.AMAZING)
            .put(PotionEffectType.RESISTANCE, E.GOOD)
            .put(PotionEffectType.SATURATION, E.AMAZING)
            .put(PotionEffectType.SLOW_FALLING, E.USELESS)
            .put(PotionEffectType.SLOWNESS, E.INCONVENIENT) // negative
            .put(PotionEffectType.SPEED, E.GOOD)
            .put(PotionEffectType.STRENGTH, E.GOOD)
            // TRIAL_OMEN: duplicates BAD_OMEN
            .put(PotionEffectType.UNLUCK, E.INCONVENIENT) // negative
            .put(PotionEffectType.WATER_BREATHING, E.USELESS)
            .put(PotionEffectType.WEAKNESS, E.INCONVENIENT) // negative
            .put(PotionEffectType.WEAVING, E.INCONVENIENT) // negative
            .put(PotionEffectType.WIND_CHARGED, E.INCONVENIENT) // negative
            .put(PotionEffectType.WITHER, E.PAINFUL) // negative
            .build();

    @SuppressWarnings({"unused"})
    @EventHandler(priority = EventPriority.NORMAL)
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (!event.isCancelled()) {
            final Projectile projectile = event.getEntity();
            final ProjectileSource archer = projectile.getShooter();
            if ((projectile instanceof Arrow)
                    && (archer instanceof LivingEntity)
                    && !(archer instanceof Player)) {

                final LivingEntity entity = (LivingEntity)archer;

                final Set<PotionEffect> potionEffects = getMetadataOrDefault(plugin, entity, MK_EFFECTS, () -> {
                    // Called once per entity life: empty lists are also cached in metadata
                    final Set<PotionEffect> newPotionEffects = new HashSet<>();
                    for (Map.Entry<PotionEffectType, E> entry : effectMap.entrySet()) {
                        if (RandomUtils.PASS(getPercentage(entity, entry.getValue().isPositive()))) {
                            newPotionEffects.add(
                                    new PotionEffect(entry.getKey(),
                                            seconds2ticks(entry.getValue().getDuration()),
                                            entry.getValue().getAmplifier()));

                            if(customLogger.isDebugMode()) {
                                customLogger.debug(String.format("%s of %s got %s",
                                        projectile.getType(), format(entity), entry.getKey().getName()));
                            }
                        }
                    }

                    return newPotionEffects;
                });

                for(final PotionEffect potionEffect : potionEffects) {
                    ((Arrow) projectile).addCustomEffect(potionEffect, true);
                }
            }
        }
    }

    private double getPercentage(final LivingEntity entity, final boolean positive) {
        if(positive) {
            return positivePercentage;
        } else {
            return negativePercentage * (1.0D + D2C.get(entity) / negativePercentageD2CM);
        }
    }

    public static NastyArcherListener getFromConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double positivePercentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "positive-percentage"), String.format("positive percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);
        double negativePercentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "negative-percentage"), String.format("negative percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if((positivePercentage <= 0.0D) && (negativePercentage <= 0.0D)){
            return null;
        }

        int negativePercentageD2CM = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "negative-percentage-d2cm"), String.format("negative percentage d2cm of %s", title), 1, 1_000_000);

        return new NastyArcherListener(plugin, customLogger,
                positivePercentage,
                negativePercentage,
                negativePercentageD2CM);
    }

    @Override
    public String toString() {
        return String.format("{positive-percentage: %s, " +
                        "negative-percentage: %s, " +
                        "negative-percentage-d2cm: %,d}",
                formatDoubleValue(positivePercentage),
                formatDoubleValue(negativePercentage),
                negativePercentageD2CM);
    }
}
