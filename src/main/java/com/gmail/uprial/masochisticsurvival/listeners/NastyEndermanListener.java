package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.*;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Pattern;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;

public class NastyEndermanListener implements Listener {
    private final double percentage;
    private final boolean infoLogAboutActions;
    private final String babyWorldPattern;
    private final int babyMinD2C;
    private final double babyPercentage;
    private final int percentageD2CM;

    private final CustomLogger customLogger;

    private NastyEndermanListener(final CustomLogger customLogger,
                                  final double percentage,
                                  final boolean infoLogAboutActions,
                                  final String babyWorldPattern,
                                  final int babyMinD2C,
                                  final double babyPercentage,
                                  final int percentageD2CM) {
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.infoLogAboutActions = infoLogAboutActions;
        this.babyWorldPattern = babyWorldPattern;
        this.babyMinD2C = babyMinD2C;
        this.babyPercentage = babyPercentage;
        this.percentageD2CM = percentageD2CM;
    }

    private Player getStrongestPlayer(final World world, final Enderman enderman, final boolean baby) {
        return AngerHelper.getSmallestItem(world.getPlayers(), (final Player player) -> {
            if((EndermanUtils.isAppropriatePlayer(player, baby))
                    && (!player.isFlying())
                    && (!player.isGliding())
                    && (AngerHelper.isValidPlayer(player))
                    && (AngerHelper.isSimulated(enderman.getLocation(), player))) {
                return getTargetScore(player);
            } else {
                return null;
            }
        });
    }

    private Double getTargetScore(final Player player) {
        // Stored in percentages, 500_000 actually means 5_000 full units.
        final Double damage = 0.01D * player.getStatistic(Statistic.DAMAGE_DEALT);
        final Double health = player.getHealth();

        return getScore(damage, health);
    }

    static Double getScore(final Double damage, final Double health) {
        /*
            According to https://minecraft.wiki/w/Player,
            default max player health is 20.0,
            which affects the distance damage at most for 5_000.0D * 20.0 = 100_000.0.
         */
        return - 1.0D * damage - 5_000.0D * health;
    }


    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()
                && (event.getEntity() instanceof Enderman)
                && (RandomUtils.PASS(getPercentage(event.getEntity())))) {

            final Enderman enderman = (Enderman) event.getEntity();

            final World world = enderman.getWorld();
            boolean baby = false;
            Player player = getStrongestPlayer(world, enderman, false);
            if((player == null)
                    && (RandomUtils.PASS(babyPercentage))
                    && (patternMatches(babyWorldPattern, world.getName()))
                    && (enderman.getLocation().length() > babyMinD2C)) {

                player = getStrongestPlayer(world, enderman, true);
                if(player != null) {
                    baby = true;
                }
            }

            if(player != null) {

                if(baby) {
                    EndermanUtils.setBaby(enderman);
                }

                if ((player.getEquipment() != null)
                        && (player.getEquipment().getHelmet() != null)
                        /*
                            I tested other heads, and they don't protect from Endermans:
                                CREEPER_HEAD
                                DRAGON_HEAD
                                PIGLIN_HEAD
                                PLAYER_HEAD
                                SKELETON_SKULL
                                ZOMBIE_HEAD
                                WITHER_SKELETON_SKULL
                         */
                        && (player.getEquipment().getHelmet().getType().equals(Material.CARVED_PUMPKIN))) {

                    // Enderman takes the player helmet
                    player.getEquipment().setHelmet(new ItemStack(Material.AIR));
                    enderman.setCarriedBlock(Material.CARVED_PUMPKIN.createBlockData());
                }

                enderman.teleport(player.getLocation());
                enderman.setTarget(player);

                final String message = String.format("%s%s targeted at %s with %.2f health",
                        baby ? "BABY-" : "",
                        format(enderman), format(player), player.getHealth());

                if(infoLogAboutActions) {
                    customLogger.info(message);
                } else {
                    customLogger.debug(message);
                }
            }
        }
    }

    private double getPercentage(final LivingEntity entity) {
        return percentage * (1.0D + D2C.get(entity) / percentageD2CM);
    }

    static boolean patternMatches(final String pattern, final String name) {
        return Pattern.matches(pattern, name);
    }

    public static NastyEndermanListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if(percentage <= 0.0D) {
            return null;
        }

        boolean infoLogAboutActions = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "info-log-about-actions"), String.format("'info-log-about-actions' flag of %s", title));

        String babyWorldPattern = ConfigReaderSimple.getString(config,
                joinPaths(key, "baby-world-pattern"), String.format("baby world pattern of %s", title));
        int babyMinD2C = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "baby-min-d2c"), String.format("baby min d2c of %s", title), 0, 1_000_000);
        double babyPercentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "baby-percentage"), String.format("baby percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);
        int percentageD2CM = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "percentage-d2cm"), String.format("percentage d2cm of %s", title), 1, 1_000_000);

        return new NastyEndermanListener(customLogger,
                percentage,
                infoLogAboutActions,
                babyWorldPattern,
                babyMinD2C,
                babyPercentage,
                percentageD2CM);
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, " +
                        "info-log-about-actions: %b, " +
                        "baby-world-pattern: %s, " +
                        "baby-min-d2c: %,d, " +
                        "baby-percentage: %s, " +
                        "percentage-d2cm: %,d}",
                formatDoubleValue(percentage),
                infoLogAboutActions,
                babyWorldPattern,
                babyMinD2C,
                formatDoubleValue(babyPercentage),
                percentageD2CM);
    }
}