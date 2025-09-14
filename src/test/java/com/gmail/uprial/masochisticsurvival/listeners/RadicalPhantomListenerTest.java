package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.MasochisticSurvival;
import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Phantom;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.RadicalPhantomListener.getFromConfig;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RadicalPhantomListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        RadicalPhantomListener listener = getFromConfig(null, getPreparedConfig(
                        "rp:",
                        "  percentage: 1.0",
                        "  power: 3.0",
                        "  cooldown: 5",
                        "  info-log-about-actions: true"),
                getParanoiacCustomLogger(), "rp", "'rp'");
        assertNotNull(listener);
        assertEquals("{percentage: 1, power: 3, cooldown: 5, info-log-about-actions: true}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        RadicalPhantomListener listener = getFromConfig(null, getPreparedConfig(
                        "rp:",
                        " percentage: 0"),
                getDebugFearingCustomLogger(), "rp", "'rp'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'rp'");
        getFromConfig(null, getPreparedConfig(
                        "?:"),
                getCustomLogger(), "rp", "'rp'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A percentage of 'rp' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "rp:",
                        " percentage: v"),
                getCustomLogger(), "rp", "'rp'");
    }

    @Test
    public void testWrongPower() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A power of 'rp' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "rp:",
                        " percentage: 1.0",
                        " power: v"),
                getCustomLogger(), "rp", "'rp'");
    }

    @Test
    public void testWrongCooldown() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A cooldown of 'rp' is not an integer");
        getFromConfig(null, getPreparedConfig(
                        "rp:",
                        " percentage: 1.0",
                        " power: 3.0",
                        " cooldown: v"),
                getCustomLogger(), "rp", "'rp'");
    }

    @Test
    public void testWrongInfoLogAboutActions() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Invalid 'info-log-about-actions' flag of 'rp'");
        getFromConfig(null, getPreparedConfig(
                        "rp:",
                        " percentage: 1.0",
                        " power: 3.0",
                        " cooldown: 5",
                        " info-log-about-actions: v"),
                getCustomLogger(), "rp", "'rp'");
    }

    @Test
    public void testDamagedByProjectileWithoutSource() {
        final MasochisticSurvival plugin = mock(MasochisticSurvival.class);

        final RadicalPhantomListener listener = new RadicalPhantomListener(
                plugin,
                getDebugFearingCustomLogger(),
                100.0D,
                3.0D,
                0,
                false
        );
        final World world = mock(World.class);
        when(world.getName()).thenReturn("w");

        final Location location = mock(Location.class);
        when(location.getX()).thenReturn(1.0D);
        when(location.getY()).thenReturn(2.0D);
        when(location.getZ()).thenReturn(3.0D);

        final Entity damager = mock(Projectile.class);
        when(damager.getWorld()).thenReturn(world);
        when(damager.getLocation()).thenReturn(location);
        when(damager.getType()).thenReturn(EntityType.ARROW);

        final Entity damagee = mock(Phantom.class);
        when(damagee.getWorld()).thenReturn(world);
        when(damagee.getLocation()).thenReturn(location);
        when(damagee.getType()).thenReturn(EntityType.PHANTOM);

        e.expect(RuntimeException.class);
        e.expectMessage("PHANTOM[w:1:2:3] damaged by ARROW[w:1:2:3] launched by null exploded with power 3.0");

        listener.onEntityDamageByEntity(new EntityDamageByEntityEvent(
                damager,
                damagee,
                EntityDamageEvent.DamageCause.PROJECTILE,
                DamageSource.builder(DamageType.ARROW).build(),
                1.0D
        ));
    }
}