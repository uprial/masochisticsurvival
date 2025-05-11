package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.NastyEnderDragonListener.getFromConfig;
import static org.junit.Assert.*;

public class NastyEnderDragonListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        NastyEnderDragonListener listener = getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true",
                        "  world-name: world_the_end",
                        "  resurrection-interval-in-s: 30",
                        "  explosion-damage-limit-per-s: 50.0",
                        "  explosion-damage-reduction: 5.0",
                        "  balls-interval-in-s: 3",
                        "  regen-multiplier: 2.0"),
                getParanoiacCustomLogger(), "ned", "'ned'");
        assertNotNull(listener);
        assertEquals("{world-name: world_the_end, " +
                        "resurrection-interval-in-s: 30, " +
                        "explosion-damage-limit-per-s: 50, " +
                        "explosion-damage-reduction: 5, " +
                        "balls-interval-in-s: 3, " +
                        "regen-multiplier: 2}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        NastyEnderDragonListener listener = getFromConfig(null, getPreparedConfig(
                        "ned:",
                        " enabled: false"),
                getDebugFearingCustomLogger(), "ned", "'ned'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty 'enabled' flag of 'ned'");
        getFromConfig(null, getPreparedConfig(
                        "?:"),
                getCustomLogger(), "ned", "'ned'");
    }

    @Test
    public void testEmptyWorldName() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Null world name of 'ned'");
        getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true"),
                getCustomLogger(), "ned", "'ned'");
    }

    @Test
    public void testWrongWorldName() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty world name of 'ned'");
        getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true",
                        "  world-name: ''"),
                getCustomLogger(), "ned", "'ned'");
    }

    @Test
    public void testWrongResurrectionIntervalInS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A resurrection interval in s of 'ned' is not an integer");
        getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true",
                        "  world-name: world_the_end",
                        "  resurrection-interval-in-s: 30.0"),
                getCustomLogger(), "ned", "'ned'");
    }

    @Test
    public void testWrongExplosionDamageLimitPerS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A explosion damage limit per s of 'ned' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true",
                        "  world-name: world_the_end",
                        "  resurrection-interval-in-s: 30",
                        "  explosion-damage-limit-per-s: x"),
                getCustomLogger(), "ned", "'ned'");
    }

    @Test
    public void testWrongExplosionDamageReduction() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A explosion damage reduction of 'ned' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true",
                        "  world-name: world_the_end",
                        "  resurrection-interval-in-s: 30",
                        "  explosion-damage-limit-per-s: 50.0",
                        "  explosion-damage-reduction: x"),
                getCustomLogger(), "ned", "'ned'");
    }

    @Test
    public void testWrongBallsIntervalInS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A balls interval in s of 'ned' is not an integer");
        getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true",
                        "  world-name: world_the_end",
                        "  resurrection-interval-in-s: 30",
                        "  explosion-damage-limit-per-s: 50.0",
                        "  explosion-damage-reduction: 5.0",
                        "  balls-interval-in-s: 3.0"),
                getCustomLogger(), "ned", "'ned'");
    }

    @Test
    public void testWrongRegenMultiplier() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A regen multiplier of 'ned' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "ned:",
                        "  enabled: true",
                        "  world-name: world_the_end",
                        "  resurrection-interval-in-s: 30",
                        "  explosion-damage-limit-per-s: 50.0",
                        "  explosion-damage-reduction: 5.0",
                        "  balls-interval-in-s: 3",
                        "  regen-multiplier: x"),
                getCustomLogger(), "ned", "'ned'");
    }

}