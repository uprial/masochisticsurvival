package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.MasochisticSurvival;
import com.gmail.uprial.masochisticsurvival.common.AngerHelper;
import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.common.TakeAimAdapter;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.google.common.collect.ImmutableSet;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;
import static com.gmail.uprial.masochisticsurvival.common.Utils.seconds2ticks;

public class AngryShooterListener implements Listener, TimeListener {
    private final double percentage;
    private final int tryAngeringIntervalInS;

    private final MasochisticSurvival plugin;
    private final CustomLogger customLogger;

    private final BukkitRunnable task;

    private final static Set<EntityType> ENTITY_TYPES = ImmutableSet.<EntityType>builder()
            .add(EntityType.BLAZE)
            .add(EntityType.BOGGED)
            .add(EntityType.BREEZE)
            .add(EntityType.CREEPER)
            .add(EntityType.GHAST)
            .add(EntityType.SKELETON)
            .add(EntityType.STRAY)
            .add(EntityType.WITCH)
            .build();

    public AngryShooterListener(final MasochisticSurvival plugin,
                                final CustomLogger customLogger,
                                final double percentage,
                                final int tryAngeringIntervalInS) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.tryAngeringIntervalInS = tryAngeringIntervalInS;

        task = new BukkitRunnable() {
            @Override
            public void run() {
                trigger();
            }
        };
    }

    @Override
    public void register() {
        if(tryAngeringIntervalInS > 0) {
            task.runTaskTimer(plugin, seconds2ticks(tryAngeringIntervalInS), seconds2ticks(tryAngeringIntervalInS));
        }
    }

    @Override
    public void unregister() {
        if(tryAngeringIntervalInS > 0) {
            task.cancel();
        }
    }

    public void trigger() {
        final Map<UUID, List<Player>> worldsPlayers = new HashMap<>();
        for(final Player player : plugin.getServer().getOnlinePlayers()) {
            if(AngerHelper.isValidPlayer(player)) {
                worldsPlayers
                        .computeIfAbsent(player.getWorld().getUID(), (k) -> new ArrayList<>())
                        .add(player);
            }
        }
        if(worldsPlayers.isEmpty()) {
            return;
        }

        for(final World world : plugin.getServer().getWorlds()) {
            if (worldsPlayers.containsKey(world.getUID())) {
                for (final Mob mob : world.getEntitiesByClass(Mob.class)) {
                    if (mob.isValid()
                            && (RandomUtils.PASS(percentage))
                            && ENTITY_TYPES.contains(mob.getType())) {

                        tryAngering(mob,
                                worldsPlayers.get(world.getUID()));
                    }
                }
            }
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()
                && (event.getEntity() instanceof Mob)
                && (RandomUtils.PASS(percentage))
                && ENTITY_TYPES.contains(event.getEntity().getType())) {

            tryAngering((Mob)event.getEntity(),
                    event.getEntity().getWorld().getEntitiesByClass(Player.class));
        }
    }

    private void tryAngering(final Mob mob, final Collection<Player> players) {
        final Player player = getClosestVisiblePlayer(mob, players);

        if(player != null) {
            TakeAimAdapter.setTarget(mob, player);

            if (customLogger.isDebugMode()) {
                customLogger.debug(String.format("%s targeted at %s", format(mob), format(player)));
            }
        }
    }

    private Player getClosestVisiblePlayer(final Mob mob, final Collection<Player> players) {
        return AngerHelper.getSmallestItem(players, (final Player player) -> {
            if(AngerHelper.isValidPlayer(player) && isMonsterSeeingPlayer(mob, player)) {
                return TakeAimAdapter.getAimPoint(player).distance(getLaunchPoint(mob));
            } else {
                return null;
            }
        });
    }

    private boolean isMonsterSeeingPlayer(final Mob mob, final Player player) {
        return
                // Don't anger entities across not simulated by the player
                (AngerHelper.isSimulated(mob, player))
                // Check for direct vision
                && (null == AngerHelper.rayTraceBlocks(
                        getLaunchPoint(mob),
                        TakeAimAdapter.getAimPoint(player),
                        FluidCollisionMode.ALWAYS));
    }

    public static AngryShooterListener getFromConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if(percentage <= 0.0D) {
            return null;
        }

        int tryAngeringIntervalInS = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "try-angering-interval-in-s"), String.format("try angering interval in s of %s", title), 0, 300);


        return new AngryShooterListener(plugin, customLogger, percentage, tryAngeringIntervalInS);
    }

    private Location getLaunchPoint(final Mob mob) {
        return mob.getEyeLocation();
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, " +
                        "try-angering-interval-in-s: %d}",
                formatDoubleValue(percentage),
                tryAngeringIntervalInS);
    }
}
