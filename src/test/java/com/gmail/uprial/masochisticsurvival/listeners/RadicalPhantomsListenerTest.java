package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.RadicalPhantomListener.getFromConfig;
import static org.junit.Assert.*;

public class RadicalPhantomsListenerTest extends TestConfigBase {
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
}