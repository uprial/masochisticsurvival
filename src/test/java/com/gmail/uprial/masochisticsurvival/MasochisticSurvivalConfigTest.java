package com.gmail.uprial.masochisticsurvival;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.bukkit.configuration.InvalidConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class MasochisticSurvivalConfigTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testEmptyDebug() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("Empty 'debug' flag. Use default value false");
        MasochisticSurvivalConfig.isDebugMode(getPreparedConfig(""), getDebugFearingCustomLogger());
    }

    @Test
    public void testNormalDebug() throws Exception {
        assertTrue(MasochisticSurvivalConfig.isDebugMode(getPreparedConfig("debug: true"), getDebugFearingCustomLogger()));
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'nasty-enderman'");
        loadConfig(getDebugFearingCustomLogger(), "");
    }

    @Test
    public void testNotMap() throws Exception {
        e.expect(InvalidConfigurationException.class);
        e.expectMessage("Top level is not a Map.");
        loadConfig("x");
    }

    @Test
    public void testDisabledConfigs() throws Exception {
        assertEquals(
                "{nasty-enderman=null, " +
                        "nasty-archer=null, " +
                        "angry-shooter=null, " +
                        "nasty-ender-dragon=null, " +
                        "explosive-shooter=null, " +
                        "greedy-villager=null, " +
                        "limit-elytras=null, " +
                        "hydra-spiders=null}",
                loadConfig(getCustomLogger(), "debug: false",
                        "nasty-enderman:",
                        "  percentage: 0.0",
                        "nasty-archer:",
                        "  positive-percentage: 0.0",
                        "  negative-percentage: 0.0",
                        "angry-shooter:",
                        "  percentage: 0",
                        "nasty-ender-dragon:",
                        "  enabled: false",
                        "explosive-shooter:",
                        "  percentage: 0.0",
                        "greedy-villager:",
                        "  replace-protection: false",
                        "  overprice-mending: false",
                        "limit-elytras:",
                        "  enabled: false",
                        "hydra-spiders:",
                        "  percentage: 0.0").toString());
    }

    @Test
    public void testNormalConfig() throws Exception {
        assertEquals(
                "{nasty-enderman={percentage: 0.1}, " +
                        "nasty-archer={positive-percentage: 0.3, negative-percentage: 1}, " +
                        "angry-shooter={percentage: 100, try-angering-interval-in-s: 30}, " +
                        "nasty-ender-dragon={world-name: world_the_end, " +
                        "resurrection-interval-in-s: 30, resurrection-amount: 2, " +
                        "explosion-damage-limit-per-s: 50, explosion-damage-reduction: 5, " +
                        "balls-interval-in-s: 3, regen-multiplier: 2}, " +
                        "explosive-shooter={percentage: 1, power: 2}, " +
                        "greedy-villager={replace-protection: true, overprice-mending: true}, " +
                        "limit-elytras={initial-height-excess: 10, height-per-freeze-second: 3}, " +
                        "hydra-spiders={percentage: 5, amount: 3, scale: 0.7, speed: 1.3}}",
                loadConfig(getCustomLogger(), "debug: false",
                        "nasty-enderman:",
                        "  percentage: 0.1",
                        "nasty-archer:",
                        "  positive-percentage: 0.3",
                        "  negative-percentage: 1.0",
                        "angry-shooter:",
                        "  percentage: 100",
                        "  try-angering-interval-in-s: 30",
                        "nasty-ender-dragon:",
                        "  enabled: true",
                        "  world-name: world_the_end",
                        "  resurrection-interval-in-s: 30",
                        "  resurrection-amount: 2",
                        "  explosion-damage-limit-per-s: 50.0",
                        "  explosion-damage-reduction: 5.0",
                        "  balls-interval-in-s: 3",
                        "  regen-multiplier: 2.0",
                        "explosive-shooter:",
                        "  percentage: 1.0",
                        "  power: 2.0",
                        "greedy-villager:",
                        "  replace-protection: true",
                        "  overprice-mending: true",
                        "limit-elytras:",
                        "  enabled: true",
                        "  initial-height-excess: 10",
                        "  height-per-freeze-second: 3",
                        "hydra-spiders:",
                        "  percentage: 5.0",
                        "  amount: 3",
                        "  scale: 0.7",
                        "  speed: 1.3").toString());
    }
}