package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.*;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.ItemStack;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;

public class NastyEndermanListener implements Listener {
    private final double percentage;
    private final boolean infoLogAboutActions;
    private final String babyWorldName;
    private final int babyDistanceToCenter;
    private final double babyPercentage;

    private final CustomLogger customLogger;

    private NastyEndermanListener(final CustomLogger customLogger,
                                  final double percentage,
                                  final boolean infoLogAboutActions,
                                  final String babyWorldName,
                                  final int babyDistanceToCenter,
                                  final double babyPercentage) {
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.infoLogAboutActions = infoLogAboutActions;
        this.babyWorldName = babyWorldName;
        this.babyDistanceToCenter = babyDistanceToCenter;
        this.babyPercentage = babyPercentage;
    }

    private Player getStrongestPlayer(final World world, final boolean baby) {
        return AngerHelper.getSmallestItem(world.getPlayers(), (final Player player) -> {
            if((EndermanUtils.isAppropriatePlayer(player, baby))
                    && (!player.isFlying())
                    && (!player.isGliding())
                    && (AngerHelper.isValidPlayer(player))) {
                return -player.getHealth();
            } else {
                return null;
            }
        });
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()
                && (event.getEntity() instanceof Enderman)
                && (RandomUtils.PASS(percentage))) {

            final World world = event.getEntity().getWorld();
            boolean baby = false;
            Player player = getStrongestPlayer(world, false);
            if((player == null)
                    && (RandomUtils.PASS(babyPercentage))
                    && (world.getName().equals(babyWorldName))
                    && (event.getEntity().getLocation().length() > babyDistanceToCenter)) {

                player = getStrongestPlayer(world, true);
                if(player != null) {
                    baby = true;
                }
            }

            if(player != null) {
                final Enderman enderman = (Enderman) event.getEntity();

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
                        baby ? "(baby)" : "",
                        format(enderman), format(player), player.getHealth());

                if(infoLogAboutActions) {
                    customLogger.info(message);
                } else {
                    customLogger.debug(message);
                }
            }
        }
    }

    public static NastyEndermanListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if(percentage <= 0.0D) {
            return null;
        }

        boolean infoLogAboutActions = ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "info-log-about-actions"), String.format("'info-log-about-actions' flag of %s", title));

        String babyWorldName = ConfigReaderSimple.getString(config,
                joinPaths(key, "baby-world-name"), String.format("baby world name of %s", title));
        int babyDistanceToCenter = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "baby-distance-to-center"), String.format("baby distance to center of %s", title), 0, 1_000_000);
        double babyPercentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "baby-percentage"), String.format("baby percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        return new NastyEndermanListener(customLogger,
                percentage,
                infoLogAboutActions,
                babyWorldName,
                babyDistanceToCenter,
                babyPercentage);
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, " +
                        "info-log-about-actions: %b, " +
                        "baby-world-name: %s, " +
                        "baby-distance-to-center: %,d, " +
                        "baby-percentage: %s}",
                formatDoubleValue(percentage),
                infoLogAboutActions,
                babyWorldName,
                babyDistanceToCenter,
                formatDoubleValue(babyPercentage));
    }
}