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
        AngryShooterListener listener = getFromConfig(getPreparedConfig(
                        "as:",
                        "  percentage: 100"),
                getParanoiacCustomLogger(), "as", "'as'");
        assertNotNull(listener);
        assertEquals("{percentage: 100}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        AngryShooterListener listener = getFromConfig(getPreparedConfig(
                        "as:",
                        " percentage: 0"),
                getDebugFearingCustomLogger(), "as", "'as'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty percentage of 'as'");
        getFromConfig(getPreparedConfig(
                        "?:"),
                getCustomLogger(), "as", "'as'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A percentage of 'as' is not a double");
        getFromConfig(getPreparedConfig(
                        "as:",
                        " percentage: v"),
                getCustomLogger(), "as", "'as'");
    }
}