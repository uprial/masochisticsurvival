package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.ExplosiveShooterListener.getFromConfig;
import static org.junit.Assert.*;

public class ExplosiveShooterListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        ExplosiveShooterListener listener = getFromConfig(null, getPreparedConfig(
                        "es:",
                        "  percentage: 1.0",
                        "  power: 2.0"),
                getParanoiacCustomLogger(), "es", "'es'");
        assertNotNull(listener);
        assertEquals("{percentage: 1, power: 2}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        ExplosiveShooterListener listener = getFromConfig(null, getPreparedConfig(
                        "es:",
                        " percentage: 0",
                        " power: 2.0"),
                getDebugFearingCustomLogger(), "es", "'es'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'es'");
        getFromConfig(null, getPreparedConfig(
                        "?:"),
                getCustomLogger(), "es", "'es'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A percentage of 'es' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "es:",
                        " percentage: v"),
                getCustomLogger(), "es", "'es'");
    }

    @Test
    public void testWrongPower() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A power of 'es' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "es:",
                        " percentage: 1.0",
                        " power: v"),
                getCustomLogger(), "es", "'es'");
    }
}