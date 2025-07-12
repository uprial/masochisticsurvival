package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.AngryShooterListener.getFromConfig;
import static org.junit.Assert.*;

public class AngryShooterListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        AngryShooterListener listener = getFromConfig(null, getPreparedConfig(
                        "as:",
                        "  percentage: 100",
                        "  try-angering-interval-in-s: 30"),
                getParanoiacCustomLogger(), "as", "'as'");
        assertNotNull(listener);
        assertEquals("{percentage: 100, try-angering-interval-in-s: 30}",
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
}