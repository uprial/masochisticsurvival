package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.HydraSpiderListener.getFromConfig;
import static org.junit.Assert.*;

public class HydraSpiderListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        HydraSpiderListener listener = getFromConfig(getPreparedConfig(
                        "es:",
                        "  percentage: 5.0",
                        "  amount: 3",
                        "  scale: 0.7",
                        "  speed: 1.3",
                        "  health: 0.7"),
                getParanoiacCustomLogger(), "es", "'es'");
        assertNotNull(listener);
        assertEquals("{percentage: 5, amount: 3, scale: 0.7, speed: 1.3, health: 0.7}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        HydraSpiderListener listener = getFromConfig(getPreparedConfig(
                        "hs:",
                        " percentage: 0"),
                getDebugFearingCustomLogger(), "hs", "'hs'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'hs'");
        getFromConfig(getPreparedConfig(
                        "?:"),
                getCustomLogger(), "hs", "'hs'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A percentage of 'hs' is not a double");
        getFromConfig(getPreparedConfig(
                        "hs:",
                        " percentage: v"),
                getCustomLogger(), "hs", "'hs'");
    }

    @Test
    public void testWrongAmount() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A amount of 'hs' is not an integer");
        getFromConfig(getPreparedConfig(
                        "hs:",
                        " percentage: 5.0",
                        " amount: v"),
                getCustomLogger(), "hs", "'hs'");
    }

    @Test
    public void testWrongScale() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A scale of 'hs' is not a double");
        getFromConfig(getPreparedConfig(
                        "hs:",
                        " percentage: 5.0",
                        " amount: 3",
                        " scale: v"),
                getCustomLogger(), "hs", "'hs'");
    }

    @Test
    public void testWrongSpeed() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A speed of 'hs' is not a double");
        getFromConfig(getPreparedConfig(
                        "hs:",
                        " percentage: 5.0",
                        " amount: 3",
                        " scale: 0.7",
                        " speed: v"),
                getCustomLogger(), "hs", "'hs'");
    }

    @Test
    public void testWrongHealth() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A health of 'hs' is not a double");
        getFromConfig(getPreparedConfig(
                        "hs:",
                        " percentage: 5.0",
                        " amount: 3",
                        " scale: 0.7",
                        " speed: 1.3",
                        " health: v"),
                getCustomLogger(), "hs", "'hs'");
    }
}