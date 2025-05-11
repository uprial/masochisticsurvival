package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.NastyShooterListener.getFromConfig;
import static org.junit.Assert.*;

public class NastyShooterListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        NastyShooterListener listener = getFromConfig(getPreparedConfig(
                        "ns:",
                        "  percentage: 100"),
                getParanoiacCustomLogger(), "ns", "'ns'");
        assertNotNull(listener);
        assertEquals("{percentage: 100}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        NastyShooterListener listener = getFromConfig(getPreparedConfig(
                        "ns:",
                        " percentage: 0"),
                getDebugFearingCustomLogger(), "ns", "'ns'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'ns'");
        getFromConfig(getPreparedConfig(
                        "?:"),
                getCustomLogger(), "ns", "'ns'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A percentage of 'ns' is not a double");
        getFromConfig(getPreparedConfig(
                        "ns:",
                        " percentage: v"),
                getCustomLogger(), "ns", "'ns'");
    }
}