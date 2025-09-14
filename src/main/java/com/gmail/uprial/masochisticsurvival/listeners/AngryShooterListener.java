package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.MasochisticSurvival;
import com.gmail.uprial.masochisticsurvival.common.AngerHelper;
import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderEnums;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.google.common.collect.ImmutableMap;
import org.bukkit.FluidCollisionMode;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.*;

public class AngryShooterListener implements Listener, TimeListener {
    private final double percentage;
    private final int tryAngeringIntervalInS;
    private final int timeoutInMs;
    private final Set<GameMode> playerGameModes;

    private final MasochisticSurvival plugin;
    private final CustomLogger customLogger;

    private final BukkitRunnable task;

    /*
        According to https://minecraft.wiki/w/Mob#Hostile_mobs,
        the following mobs have a range attack.

        Additionally, Creeper exists to be annoying.
     */
    final static Map<EntityType,FluidCollisionMode> TYPE_2_MODE = ImmutableMap.<EntityType,FluidCollisionMode>builder()
            // Water stops explosions
            .put(EntityType.CREEPER, FluidCollisionMode.ALWAYS)
            // Fireballs don't care about fluids
            .put(EntityType.BLAZE, FluidCollisionMode.NEVER)
            .put(EntityType.GHAST, FluidCollisionMode.NEVER)
            // Water slows down the arrows
            .put(EntityType.BOGGED, FluidCollisionMode.ALWAYS)
            .put(EntityType.PILLAGER, FluidCollisionMode.ALWAYS)
            .put(EntityType.SKELETON, FluidCollisionMode.ALWAYS)
            .put(EntityType.STRAY, FluidCollisionMode.ALWAYS)
            // Tridents are made for water
            .put(EntityType.DROWNED, FluidCollisionMode.NEVER)
            // Levitation status has no effect underwater
            .put(EntityType.SHULKER, FluidCollisionMode.ALWAYS)
            // Aquatic monsters
            .put(EntityType.ELDER_GUARDIAN, FluidCollisionMode.NEVER)
            .put(EntityType.GUARDIAN, FluidCollisionMode.NEVER)
            // Tested that wind charges work good underwater
            .put(EntityType.BREEZE, FluidCollisionMode.NEVER)
            // Tested that Vexes don't swim deep
            .put(EntityType.EVOKER, FluidCollisionMode.ALWAYS)
            // Tested that water slows down the potions
            .put(EntityType.WITCH, FluidCollisionMode.ALWAYS)
            .build();

    public AngryShooterListener(final MasochisticSurvival plugin,
                                final CustomLogger customLogger,
                                final double percentage,
                                final int tryAngeringIntervalInS,
                                final int timeoutInMs,
                                final Set<GameMode> playerGameModes) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.percentage = percentage;
        this.tryAngeringIntervalInS = tryAngeringIntervalInS;
        this.timeoutInMs = timeoutInMs;
        this.playerGameModes = playerGameModes;

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
        final long start = System.currentTimeMillis();

        int processed = 0;
        int appropriate = 0;
        int total = 0;

        final Map<UUID, List<Player>> worldsPlayers = new HashMap<>();
        for(final Player player : plugin.getServer().getOnlinePlayers()) {
            if(isValidPlayer(player)) {
                worldsPlayers
                        .computeIfAbsent(player.getWorld().getUID(), (k) -> new ArrayList<>())
                        .add(player);
            }
        }
        if (!worldsPlayers.isEmpty()) {
            for (final World world : plugin.getServer().getWorlds()) {
                if (worldsPlayers.containsKey(world.getUID())) {
                    // Filtering not by Mob, to reduce the number of records
                    for (final Enemy enemy : world.getEntitiesByClass(Enemy.class)) {
                        if (TYPE_2_MODE.containsKey(enemy.getType())
                                && enemy.isValid()) {

                            if(RandomUtils.PASS(percentage)
                                    && tryAngering((Mob)enemy, worldsPlayers.get(world.getUID()))) {
                                processed++;
                            }
                            appropriate++;
                        }
                        total++;
                    }
                }
            }
        }

        final long end = System.currentTimeMillis();
        if(end - start >= timeoutInMs) {
            customLogger.warning(String.format("AngryShooter cron took %dms, angered %d/%d/%d enemies",
                    end - start, processed, appropriate, total));
        }
    }

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()
                && (event.getEntity() instanceof Mob)
                && (RandomUtils.PASS(percentage))
                && TYPE_2_MODE.containsKey(event.getEntity().getType())) {

            final Collection<Player> players = event.getEntity().getWorld().getPlayers();
            players.removeIf(player -> !isValidPlayer(player));
            tryAngering((Mob)event.getEntity(), players);
        }
    }

    private boolean isValidPlayer(final Player player) {
        return AngerHelper.isValidPlayer(player) && playerGameModes.contains(player.getGameMode());
    }

    private boolean tryAngering(final Mob mob, final Collection<Player> players) {
        final Player player = getMostVulnerableVisiblePlayer(mob, players);

        if((player != null)
                && ((mob.getTarget() == null)
                || !mob.getTarget().getUniqueId().equals(player.getUniqueId()))) {

            TakeAimAdapter.setTarget(mob, player,
                    EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
                    (final Mob _mob, final Player _player) -> {
                        if (customLogger.isDebugMode()) {
                            customLogger.debug(String.format("%s targeted at %s", format(mob), format(player)));
                        }
                    });

            return true;
        } else {
            return false;
        }
    }

    private Player getMostVulnerableVisiblePlayer(final Mob mob, final Collection<Player> players) {
        return AngerHelper.getSmallestItem(players, (final Player player) -> {
            /*
                this.isValidPlayer(player)
                is already checked in trigger()
                and onCreatureSpawn()
             */
            if(isMonsterSeeingPlayer(mob, player)) {
                return getMobTargetScore(mob, player);
            } else {
                return null;
            }
        });
    }

    private Double getMobTargetScore(final Mob mob, final Player player) {
        final Double distance = TakeAimAdapter.getAimPoint(player).distance(getLaunchPoint(mob));
        final Double health = player.getHealth();

        return getScore(distance, health);
    }

    static Double getScore(final Double distance, final Double health) {
        /*
            According to https://minecraft.wiki/w/Player,
            default max player health is 20.0,
            which affects the distance weight at most for 2.5D * 20.0 = 50.0.
         */
        return 1.0D * distance + 2.5D * health;
    }

    private boolean isMonsterSeeingPlayer(final Mob mob, final Player player) {
        return
                // Don't anger entities that are not simulated by the player
                (AngerHelper.isSimulated(mob.getLocation(), player))
                // Check for direct vision
                && (null == AngerHelper.rayTraceBlocks(
                        getLaunchPoint(mob),
                        TakeAimAdapter.getAimPoint(player),
                        TYPE_2_MODE.get(mob.getType())));
    }

    public static AngryShooterListener getFromConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if(percentage <= 0.0D) {
            return null;
        }

        int tryAngeringIntervalInS = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "try-angering-interval-in-s"), String.format("try angering interval in s of %s", title), 0, 300);

        int timeoutInMs = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "timeout-in-ms"), String.format("timeout in ms of %s", title), 1, 3600_000);

        Set<GameMode> playerGameModes = ConfigReaderEnums.getSet(GameMode.class, config, customLogger,
                joinPaths(key, "player-game-modes"), String.format("player game modes of %s", title));

        return new AngryShooterListener(plugin, customLogger, percentage, tryAngeringIntervalInS, timeoutInMs, playerGameModes);
    }

    private Location getLaunchPoint(final Mob mob) {
        return mob.getEyeLocation();
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s, " +
                        "try-angering-interval-in-s: %d, " +
                        "timeout-in-ms: %d, " +
                        "player-game-modes: %s}",
                formatDoubleValue(percentage),
                tryAngeringIntervalInS,
                timeoutInMs,
                playerGameModes);
    }
}
