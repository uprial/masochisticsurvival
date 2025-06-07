package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.common.AngerHelper;
import com.gmail.uprial.masochisticsurvival.common.CustomLogger;
import com.gmail.uprial.masochisticsurvival.common.RandomUtils;
import com.gmail.uprial.masochisticsurvival.common.TakeAimAdapter;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.google.common.collect.ImmutableSet;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.RayTraceResult;

import java.util.Set;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;

public class AngryShooterListener implements Listener {
    private final double percentage;

    private final CustomLogger customLogger;

    public AngryShooterListener(final CustomLogger customLogger,
                                final double percentage) {
        this.customLogger = customLogger;
        this.percentage = percentage;
    }

    private final Set<EntityType> entityTypes = ImmutableSet.<EntityType>builder()
            .add(EntityType.BLAZE)
            .add(EntityType.BOGGED)
            .add(EntityType.BREEZE)
            .add(EntityType.CREEPER)
            .add(EntityType.GHAST)
            .add(EntityType.SKELETON)
            .add(EntityType.STRAY)
            .add(EntityType.WITCH)
            .build();

    @SuppressWarnings("unused")
    @EventHandler(priority = EventPriority.NORMAL)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.isCancelled()
                && (event.getEntity() instanceof Mob)
                && (RandomUtils.PASS(percentage))
                && entityTypes.contains(event.getEntity().getType())) {

            final Mob mob = (Mob)event.getEntity();
            final Player player = getClosestVisiblePlayer(mob);

            if(player != null) {
                TakeAimAdapter.setTarget(mob, player);

                if (customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s targeted at %s", format(mob), format(player)));
                }
            }
        }
    }

    private Player getClosestVisiblePlayer(final Mob mob) {
        return AngerHelper.getSmallestItem(mob.getWorld().getEntitiesByClass(Player.class), (final Player player) -> {
            if(AngerHelper.isValidPlayer(player) && isMonsterSeeingPlayer(mob, player)) {
                return TakeAimAdapter.getAimPoint(player).distance(TakeAimAdapter.getAimPoint(mob));
            } else {
                return null;
            }
        });
    }

    private boolean isMonsterSeeingPlayer(final Mob mob, final Player player) {
        final Location fromLocation = TakeAimAdapter.getAimPoint(mob);
        final Location toLocation = TakeAimAdapter.getAimPoint(player);
        // Check for direct vision
        final RayTraceResult rayTraceResult = AngerHelper.rayTraceBlocks(
                fromLocation,
                toLocation,
                FluidCollisionMode.ALWAYS);

        return (rayTraceResult == null);
    }

    public static AngryShooterListener getFromConfig(FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        double percentage = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "percentage"), String.format("percentage of %s", title), 0.0D, RandomUtils.MAX_PERCENT);

        if(percentage <= 0.0D) {
            return null;
        }

        return new AngryShooterListener(customLogger, percentage);
    }

    @Override
    public String toString() {
        return String.format("{percentage: %s}", formatDoubleValue(percentage));
    }
}
