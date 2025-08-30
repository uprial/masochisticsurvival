package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.NastyArcherListener.getFromConfig;
import static org.junit.Assert.*;

public class NastyArcherListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        NastyArcherListener listener = getFromConfig(null, getPreparedConfig(
                        "na:",
                        "  positive-percentage: 0.3",
                        "  negative-percentage: 1.0",
                        "  negative-percentage-d2cm: 2_000"),
                getParanoiacCustomLogger(), "na", "'na'");
        assertNotNull(listener);
        assertEquals("{positive-percentage: 0.3, " +
                        "negative-percentage: 1, " +
                        "negative-percentage-d2cm: 2,000}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        NastyArcherListener listener = getFromConfig(null, getPreparedConfig(
                        "na:",
                        " positive-percentage: 0",
                        " negative-percentage: 0"),
                getDebugFearingCustomLogger(), "na", "'na'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty positive percentage of 'na'");
        getFromConfig(null, getPreparedConfig(
                        "?:"),
                getCustomLogger(), "na", "'na'");
    }

    @Test
    public void testWrongPositivePercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A positive percentage of 'na' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "na:",
                        " positive-percentage: v"),
                getCustomLogger(), "na", "'na'");
    }

    @Test
    public void testWrongNegativePercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A negative percentage of 'na' is not a double");
        getFromConfig(null, getPreparedConfig(
                        "na:",
                        " positive-percentage: 1",
                        " negative-percentage: v"),
                getCustomLogger(), "na", "'na'");
    }

    @Test
    public void testWrongNegativePercentageD2CM() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A negative percentage d2cm of 'na' is not an integer");
        getFromConfig(null, getPreparedConfig(
                        "na:",
                        " positive-percentage: 1",
                        " negative-percentage: 1.0",
                        " negative-percentage-d2cm: v"),
                getCustomLogger(), "na", "'na'");
    }}