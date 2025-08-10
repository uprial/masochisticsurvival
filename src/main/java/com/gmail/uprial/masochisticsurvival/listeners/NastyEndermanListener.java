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

    private final CustomLogger customLogger;

    private NastyEndermanListener(final CustomLogger customLogger,
                                  final double percentage,
                                  final boolean infoLogAboutActions) {
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.infoLogAboutActions = infoLogAboutActions;
    }

    private Player getStrongestPlayer(final World world) {
        return AngerHelper.getSmallestItem(world.getEntitiesByClass(Player.class), (final Player player) -> {
            if((EndermanUtils.isAppropriatePlayer(player))
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

            final Player player = getStrongestPlayer(event.getEntity().getWorld());

            if(player != null) {
                final Enderman enderman = (Enderman) event.getEntity();

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

                final String message = String.format("%s targeted at %s with %.2f health",
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

        return new NastyEndermanListener(customLogger, percentage, infoLogAboutActions);
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, info-log-about-actions: %b}",
                formatDoubleValue(percentage), infoLogAboutActions);
    }
}