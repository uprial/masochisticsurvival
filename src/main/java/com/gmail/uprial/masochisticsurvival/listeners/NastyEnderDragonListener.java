package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.MasochisticSurvival;
import com.gmail.uprial.masochisticsurvival.common.*;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderNumbers;
import com.gmail.uprial.masochisticsurvival.config.ConfigReaderSimple;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.google.common.collect.ImmutableSet;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.projectiles.ProjectileSource;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.gmail.uprial.masochisticsurvival.common.DoubleHelper.formatDoubleValue;
import static com.gmail.uprial.masochisticsurvival.common.Formatter.format;
import static com.gmail.uprial.masochisticsurvival.common.Utils.joinPaths;
import static com.gmail.uprial.masochisticsurvival.common.Utils.seconds2ticks;
import static java.lang.Math.*;

public class NastyEnderDragonListener implements Listener {
    private final MasochisticSurvival plugin;
    private final CustomLogger customLogger;

    private final String worldName;
    private final int resurrectionIntervalInS;
    private final int resurrectionAmount;
    private final double explosionDamageLimitPerS;
    private final double explosionDamageReduction;
    private final int ballsIntervalInS;
    private final double regenMultiplier;

    private static final Set<Location> BEDROCKS = new HashSet<>();

    public NastyEnderDragonListener(final MasochisticSurvival plugin,
                                    final CustomLogger customLogger,
                                    final String worldName,
                                    final int resurrectionIntervalInS,
                                    final int resurrectionAmount,
                                    final double explosionDamageLimitPerS,
                                    final double explosionDamageReduction,
                                    final int ballsIntervalInS,
                                    final double regenMultiplier) {
        this.plugin = plugin;
        this.customLogger = customLogger;
        this.worldName = worldName;
        this.resurrectionIntervalInS = resurrectionIntervalInS;
        this.resurrectionAmount = resurrectionAmount;
        this.explosionDamageLimitPerS = explosionDamageLimitPerS;
        this.explosionDamageReduction = explosionDamageReduction;
        this.ballsIntervalInS = ballsIntervalInS;
        this.regenMultiplier = regenMultiplier;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (isAppropriateWorld(event.getChunk().getWorld())) {
            updateBedrocksCache(event.getChunk().getWorld());
        }
    }

    /*
        According to https://minecraft.wiki/w/End_spike
        10 pillars generate in a 43-block radius circle around the exit portal,
        which is up to ceil(43 / 16) = 3 chunks.

        (34^2 + 26^2)^0.5 = 42.8 <= 43
        in chunks it's
        (3^2 + 2^2)^0.5 = 3.6 <= 4

        We also need to exclude the central bedrock above the end portal.

     */
    private static final int    PILLARS_COUNT    = 10;
    private static final double MIN_CHUNK_RADIUS = 0.5D;
    private static final double MAX_CHUNK_RADIUS = 4;
    private static final int    MAX_CHUNK_XZ     = 3;

    private static final AtomicReference<String> BEDROCKS_UPDATED = new AtomicReference<>("");

    private void updateBedrocksCache(final World world) {
        // update the cache only once
        if(!BEDROCKS_UPDATED.get().equals(worldName)) {
            BEDROCKS_UPDATED.set(worldName);

            BEDROCKS.clear();

            for (int x = -MAX_CHUNK_XZ; x <= MAX_CHUNK_XZ; x++) {
                for (int z = -MAX_CHUNK_XZ; z <= MAX_CHUNK_XZ; z++) {
                    final double distance = getDistance(x, z);
                    if ((distance > MIN_CHUNK_RADIUS) && (distance <= MAX_CHUNK_RADIUS)) {
                        final Location bedrock = searchBedrock(world.getChunkAt(x, z, true));
                        if(bedrock != null) {
                            BEDROCKS.add(bedrock);
                        }
                    }
                }
            }
            for(final Location bedrock : BEDROCKS) {
                resurrect(world, bedrock, false);
            }
            if(BEDROCKS.size() == PILLARS_COUNT) {
                customLogger.info(String.format("Detected %d of %d bedrocks",
                        BEDROCKS.size(), PILLARS_COUNT));
            } else {
                customLogger.error(String.format("Detected %d bedrocks instead of %d",
                        BEDROCKS.size(), PILLARS_COUNT));
            }
        }
    }

    private Location searchBedrock(final Chunk chunk) {
        /*
            According to https://minecraft.wiki/w/End_spike
            pillars generate between 76 and 103 Y.
         */
        final int minY = max(70, chunk.getWorld().getMinHeight());
        final int maxY = min(110, chunk.getWorld().getMaxHeight());
        // Takes 3-10ms per chunk
        for (int y = minY; y < maxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    final Block block = chunk.getBlock(x, y, z);
                    if (block.getType().equals(Material.BEDROCK)) {
                        return block.getLocation();
                    }
                }
            }
        }

        return null;
    }

    private long lastResurrection = 0;

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if(!event.isCancelled()
                && (event.getEntity() instanceof EnderDragon)
                && (isAppropriateWorld(event.getEntity().getWorld()))) {

            final long currentTime = System.currentTimeMillis();
            if((currentTime - lastResurrection) / 1_000 < resurrectionIntervalInS) {
                // Ender Dragon was attacked, but the resurrection interval hasn't passed
                return;
            }
            lastResurrection = currentTime;

            final World world = event.getEntity().getWorld();

            final Collection<EnderCrystal> crystals = world.getEntitiesByClass(EnderCrystal.class);
            crystals.removeIf(crystal -> !crystal.isValid());

            final Set<Location> bedrocksWithoutCrystals = new HashSet<>();
            for(final Location bedrock : BEDROCKS) {
                boolean found = false;
                for (final EnderCrystal crystal : crystals) {
                    if (crystal.getLocation().distance(bedrock2crystal(bedrock)) < 1.0D) {
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    bedrocksWithoutCrystals.add(bedrock);
                }
            }

            if(bedrocksWithoutCrystals.isEmpty()) {
                if (customLogger.isDebugMode()) {
                    customLogger.debug(String.format("%s attacked, but all crystals are in place", format(event.getEntity())));
                }
            } else {
                int resurrectionsRemaining = resurrectionAmount;
                do {
                    /*
                        Don't let the player predict
                        which crystal location without crystals will be resurrected.
                     */
                    final Location bedrock = RandomUtils.getSetItem(bedrocksWithoutCrystals);

                    resurrect(world, bedrock, true);

                    bedrocksWithoutCrystals.remove(bedrock);

                    resurrectionsRemaining--;

                    if (customLogger.isDebugMode()) {
                        customLogger.debug(String.format("Crystal at %s resurrected", format(bedrock)));
                    }

                } while ((resurrectionsRemaining > 0) && (!bedrocksWithoutCrystals.isEmpty()));
            }

            final Entity damager = getRealSource(event.getDamager());
            if(damager instanceof Player) {
                final Player player = (Player)damager;

                //Ender Dragon sees everything!
                //if (AngerHelper.isValidPlayer(player)) {
                launch((EnderDragon)event.getEntity(), player);

                // Target a random enderman to the player
                if (EndermanUtils.isAppropriatePlayer(player, false)) {
                        //Wait for flying or gliding players on the ground!
                        //&& (!player.isFlying())
                        //&& (!player.isGliding())) {
                    for (final Enderman enderman : world.getEntitiesByClass(Enderman.class)) {
                        if (enderman.isValid()
                                // Any distance 100 >> X >> 0 works for a random pick
                                && RandomUtils.PASS(10.0D)
                                // Don't anger entities that are not simulated by the player
                                && AngerHelper.isSimulated(enderman, player)) {
                            enderman.setTarget(player);
                            if (customLogger.isDebugMode()) {
                                customLogger.debug(String.format("Targeted %s at %s",
                                        format(enderman), format(player)));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private final Set<EntityDamageEvent.DamageCause> explosionReasons = ImmutableSet.<EntityDamageEvent.DamageCause>builder()
            .add(EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
            .add(EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
            .build();

    private final Map<UUID, Map<Integer,Double>> explosionLimits = new HashMap<>();

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityDamage(EntityDamageEvent event) {
        if(!event.isCancelled()
                && (event.getEntity() instanceof EnderDragon)
                && (isAppropriateWorld(event.getEntity().getWorld()))
                && (explosionReasons.contains(event.getCause()))) {

            final Map<Integer,Double> explosionLimit
                    = explosionLimits.computeIfAbsent(event.getEntity().getUniqueId(), (k) -> new HashMap<>());

            final Integer second = (int)(System.currentTimeMillis() / 1_000);

            final double limit
                    = explosionLimit.computeIfAbsent(second, (k) -> explosionDamageLimitPerS);

            final double oldDamage = event.getDamage();
            final double newDamage = Math.min(oldDamage / explosionDamageReduction, limit);

            explosionLimit.clear();
            explosionLimit.put(second, limit - newDamage);

            event.setDamage(newDamage);
            if (customLogger.isDebugMode()
                    && (newDamage < oldDamage)
                    // Otherwise, too many log messages will be produced.
                    && (newDamage > 0.0D)) {
                customLogger.debug(String.format("Changed explosive damage to %s from %.2f to %.2f, new limit: %.2f",
                        format(event.getEntity()), oldDamage, newDamage, limit - newDamage));
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityRegainHealth(EntityRegainHealthEvent event) {
        if(!event.isCancelled()
                && (event.getEntity() instanceof EnderDragon)
                && (isAppropriateWorld(event.getEntity().getWorld()))) {

            event.setAmount(event.getAmount() * regenMultiplier);
            /*
            Commented because too frequent

            final double oldRegen = event.getAmount();
            final double newRegen = oldRegen * REGEN_MULTIPLIER;

            event.setAmount(newRegen);

            if (customLogger.isDebugMode()) {
                customLogger.debug(String.format("Changed regen of %s from %.2f to %.2f",
                        format(event.getEntity()), oldRegen, newRegen));
            }
             */
        }
    }

    /*
        According to https://minecraft.wiki/w/End_spike,
        any blocks the player had placed within 10 blocks
        in all directions of the bedrock block at the top of the end spikes are deleted.
     */
    private static final int CLEARANCE_RADIUS = 10;
    private static final int IRON_BARS_HEIGHT = 3;
    private static final int IRON_BARS_RADIUS = 2;
    private void resurrect(final World world, final Location bedrock, final boolean update) {
        final int x = bedrock.getBlockX();
        final int y = bedrock.getBlockY();
        final int z = bedrock.getBlockZ();
        if(update) {
            world.createExplosion(bedrock2crystal(bedrock), CLEARANCE_RADIUS);
        }

        for (int dy = -CLEARANCE_RADIUS; dy < CLEARANCE_RADIUS; dy++) {
            for (int dx = -CLEARANCE_RADIUS; dx < CLEARANCE_RADIUS; dx++) {
                for (int dz = -CLEARANCE_RADIUS; dz < CLEARANCE_RADIUS; dz++) {
                    final Block block = world.getBlockAt(x + dx, y + dy, z + dz);

                    final Material material;
                    if((dx == 0) && (dy == 0) && (dz == 0)) {
                        material = Material.BEDROCK;
                    } else if ((dy < 0) && (getDistance(dx, dz) <= 3.3D)) {
                        material = Material.OBSIDIAN;
                    } else if ((dy < 0)) { // && (!block.getType().equals(Material.OBSIDIAN))) {
                        material = null;//Material.AIR;
                    } else if ((dy == IRON_BARS_HEIGHT)
                            && (abs(dx) <= IRON_BARS_RADIUS) && (abs(dz) <= IRON_BARS_RADIUS)) {
                        material = Material.IRON_BARS;
                    } else if ((dy >= 0) && (dy < IRON_BARS_HEIGHT) &&
                            (abs(dx) == IRON_BARS_RADIUS && abs(dz) <= IRON_BARS_RADIUS
                                    || abs(dx) <= IRON_BARS_RADIUS && abs(dz) == IRON_BARS_RADIUS)) {
                        material = Material.IRON_BARS;
                    } else {
                        material = Material.AIR;
                    }

                    if((material != null) && (!block.getType().equals(material))) {
                        block.setType(material);
                    }
                }
            }
        }

        if(update) {
            world.spawnEntity(bedrock2crystal(bedrock), EntityType.END_CRYSTAL);
        }
    }

    private void launch(final EnderDragon enderDragon, final Player player) {
        final int ballsCount = resurrectionIntervalInS / ballsIntervalInS;
        for(int i = 0; i < ballsCount; i++) {
            plugin.scheduleDelayed(() -> {
                if(!enderDragon.isValid() || !player.isValid()
                        || !enderDragon.getWorld().equals(player.getWorld())) {
                    return;
                }

                TakeAimAdapter.launchFireball(enderDragon, player,
                        EntityTargetEvent.TargetReason.CLOSEST_PLAYER,
                        DragonFireball.class);
            }, seconds2ticks(ballsIntervalInS * i));
        }
    }

    private static Location bedrock2crystal(final Location bedrockLocation) {
        // Center of the block above
        return bedrockLocation.clone().add(0.5D, 1.0D, 0.5D);
    }

    private boolean isAppropriateWorld(final World world) {
        return world.getName().equals(worldName);
    }

    private static Entity getRealSource(final Entity source) {
        if (source instanceof Projectile) {
            final Projectile projectile = (Projectile)source;
            final ProjectileSource projectileSource = projectile.getShooter();
            if (projectileSource instanceof Entity) {
                return (Entity)projectileSource;
            }
        }
        return source;
    }

    private static double getDistance(final int x, final int z) {
        return Math.sqrt(x * x + z * z);
    }

    public static NastyEnderDragonListener getFromConfig(MasochisticSurvival plugin, FileConfiguration config, CustomLogger customLogger, String key, String title) throws InvalidConfigException {
        if(!ConfigReaderSimple.getBoolean(config, customLogger,
                joinPaths(key, "enabled"), String.format("'enabled' flag of %s", title))) {

            return null;
        }

        String worldName = ConfigReaderSimple.getString(config,
                joinPaths(key, "world-name"), String.format("world name of %s", title));
        int resurrectionIntervalInS = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "resurrection-interval-in-s"), String.format("resurrection interval in s of %s", title), 0, 300);
        int resurrectionAmount = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "resurrection-amount"), String.format("resurrection amount of %s", title), 1, 10);
        double explosionDamageLimitPerS = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "explosion-damage-limit-per-s"), String.format("explosion damage limit per s of %s", title), 0.0D, 200.0D);
        double explosionDamageReduction = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "explosion-damage-reduction"), String.format("explosion damage reduction of %s", title), 0.0D, 200.0D);
        int ballsIntervalS = ConfigReaderNumbers.getInt(config, customLogger,
                joinPaths(key, "balls-interval-in-s"), String.format("balls interval in s of %s", title), 1, 300);
        double regenMultiplier = ConfigReaderNumbers.getDouble(config, customLogger,
                joinPaths(key, "regen-multiplier"), String.format("regen multiplier of %s", title), 0.0D, 200.0D);

        return new NastyEnderDragonListener(plugin, customLogger,
                worldName,
                resurrectionIntervalInS,
                resurrectionAmount,
                explosionDamageLimitPerS,
                explosionDamageReduction,
                ballsIntervalS,
                regenMultiplier);
    }

    @Override
    public String toString() {
        return String.format("{world-name: %s, " +
                        "resurrection-interval-in-s: %d, " +
                        "resurrection-amount: %d, " +
                        "explosion-damage-limit-per-s: %s, " +
                        "explosion-damage-reduction: %s, " +
                        "balls-interval-in-s: %d, " +
                        "regen-multiplier: %s}",
                worldName,
                resurrectionIntervalInS,
                resurrectionAmount,
                formatDoubleValue(explosionDamageLimitPerS),
                formatDoubleValue(explosionDamageReduction),
                ballsIntervalInS,
                formatDoubleValue(regenMultiplier));
    }
}