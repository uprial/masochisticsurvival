package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Mob;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
import java.util.Set;

import static com.gmail.uprial.masochisticsurvival.listeners.AngryShooterListener.*;
import static org.junit.Assert.*;

public class AngryShooterListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testInterfaces() throws Exception {
        for(final EntityType type : TYPE_2_MODE.keySet()) {
            assertTrue(String.format("%s doesn't extend Enemy", type.getEntityClass()),
                    getAllSuperInterfaces(type.getEntityClass()).contains(Enemy.class));
            assertTrue(String.format("%s doesn't extend Mob", type.getEntityClass()),
                    getAllSuperInterfaces(type.getEntityClass()).contains(Mob.class));
        }
    }

    private Set<Class<?>> getAllSuperInterfaces(final Class<?> c) {
        Set<Class<?>> interfaces = new HashSet<>();
        for(Class<?> i : c.getInterfaces()) {
            interfaces.add(i);
            interfaces.addAll(getAllSuperInterfaces(i));
        }

        return interfaces;
    }

    @Test
    public void testWhole() throws Exception {
        AngryShooterListener listener = getFromConfig(null, getPreparedConfig(
                        "as:",
                        "  percentage: 100",
                        "  try-angering-interval-in-s: 30",
                        "  timeout-in-ms: 5"),
                getParanoiacCustomLogger(), "as", "'as'");
        assertNotNull(listener);
        assertEquals("{percentage: 100, " +
                        "try-angering-interval-in-s: 30, " +
                        "timeout-in-ms: 5}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        AngryShooterListener listener = getFromConfig(null, getPreparedConfig(
                        "as:",
                        " percentage: 0"),
                getDebugFearingCustomLogger(), "as", "'as'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'as'");
        getFromConfig(null, getPreparedConfig(
                        "?:"),
                getCustomLogger(), "as", "'as'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A percentage of 'as' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "as:",
                        " percentage: v"),
                getCustomLogger(), "as", "'as'");
    }

    @Test
    public void testEmptyTryAngeringIntervalInS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty try angering interval in s of 'as'");
        getFromConfig(null, getPreparedConfig(
                        "as:",
                        "  percentage: 100"),
                getCustomLogger(), "as", "'as'");
    }

    @Test
    public void testWrongTryAngeringIntervalInS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A try angering interval in s of 'as' should be at least 0");
        getFromConfig(null, getPreparedConfig(
                        "as:",
                        "  percentage: 100",
                        "  try-angering-interval-in-s: -1"),
                getCustomLogger(), "as", "'as'");
    }

    @Test
    public void testWrongTimeoutInMS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A timeout in ms of 'as' should be at least 1");
        getFromConfig(null, getPreparedConfig(
                        "as:",
                        "  percentage: 100",
                        "  try-angering-interval-in-s: 30",
                        "  timeout-in-ms: -1"),
                getCustomLogger(), "as", "'as'");
    }

    @Test
    public void testDistanceVsHealthScore() throws Exception {
        assertTrue(getScore(10.0D, 20.0D)
                > getScore(20.0D, 10.0D));

        assertTrue(getScore(10.0D, 20.0D)
                > getScore(50.0D, 0.1D));

        assertFalse(getScore(10.0D, 20.0D)
                > getScore(70.0D, 0.1D));
    }
}