package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.NastyEndermanListener.getFromConfig;
import static org.junit.Assert.*;

public class NastyEndermanListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        NastyEndermanListener listener = getFromConfig(getPreparedConfig(
                        "ne:",
                        "  percentage: 0.033",
                        "  info-log-about-actions: true",
                        "  baby-world-name: world_the_end",
                        "  baby-distance-to-center: 3_000",
                        "  baby-percentage: 33.3"),
                getParanoiacCustomLogger(), "ne", "'ne'");
        assertNotNull(listener);
        assertEquals("{percentage: 0.033, " +
                        "info-log-about-actions: true, " +
                        "baby-world-name: world_the_end, " +
                        "baby-distance-to-center: 3,000, " +
                        "baby-percentage: 33.3}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        NastyEndermanListener listener = getFromConfig(getPreparedConfig(
                        "ne:",
                        " percentage: 0"),
                getDebugFearingCustomLogger(), "ne", "'ne'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'ne'");
        getFromConfig(getPreparedConfig(
                        "?:"),
                getCustomLogger(), "ne", "'ne'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A percentage of 'ne' is not a double");
        getFromConfig(getPreparedConfig(
                        "ne:",
                        " percentage: v"),
                getCustomLogger(), "ne", "'ne'");
    }

    @Test
    public void testWrongInfoLogAboutActions() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Invalid 'info-log-about-actions' flag of 'ne'");
        getFromConfig(getPreparedConfig(
                        "ne:",
                        " percentage: 0.033",
                        " info-log-about-actions: v"),
                getCustomLogger(), "ne", "'ne'");
    }

    @Test
    public void testWrongBabyWorldName() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Null baby world name of 'ne'");
        getFromConfig(getPreparedConfig(
                        "ne:",
                        " percentage: 0.033",
                        " info-log-about-actions: true"),
                getCustomLogger(), "ne", "'ne'");
    }

    @Test
    public void testWrongBabyDistanceToCenter() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A baby distance to center of 'ne' is not an integer");
        getFromConfig(getPreparedConfig(
                        "ne:",
                        " percentage: 0.033",
                        " info-log-about-actions: true",
                        " baby-world-name: world_the_end",
                        " baby-distance-to-center: v"),
                getCustomLogger(), "ne", "'ne'");
    }

    @Test
    public void testWrongBabyPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A baby percentage of 'ne' is not a double");
        getFromConfig(getPreparedConfig(
                        "ne:",
                        " percentage: 0.033",
                        " info-log-about-actions: true",
                        " baby-world-name: world_the_end",
                        " baby-distance-to-center: 3_000",
                        " baby-percentage: v"),
                getCustomLogger(), "ne", "'ne'");
    }
}