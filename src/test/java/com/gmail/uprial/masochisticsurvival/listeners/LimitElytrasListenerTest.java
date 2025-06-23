package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.LimitElytrasListener.getFromConfig;
import static org.junit.Assert.*;

public class LimitElytrasListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        LimitElytrasListener listener = getFromConfig(getPreparedConfig(
                        "le:",
                        " enabled: true",
                        " world-max-height-excess: 50",
                        " freeze-ticks-in-s: 5"),
                getParanoiacCustomLogger(), "le", "'le'");
        assertNotNull(listener);
        assertEquals("{world-max-height-excess: 50, freeze-ticks-in-s: 5}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        LimitElytrasListener listener = getFromConfig(getPreparedConfig(
                        "le:",
                        " enabled: false"),
                getDebugFearingCustomLogger(), "le", "'le'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty 'enabled' flag of 'le'");
        getFromConfig(getPreparedConfig(
                        "?:"),
                getCustomLogger(), "le", "'le'");
    }

    @Test
    public void testWorldMaxHeightExcess() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A world max height excess of 'le' is not an integer");
        getFromConfig(getPreparedConfig(
                        "le:",
                        " enabled: true",
                        " world-max-height-excess: v"),
                getCustomLogger(), "le", "'le'");
    }

    @Test
    public void testFreezeTicksInS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A freeze ticks in s of 'le' is not an integer");
        getFromConfig(getPreparedConfig(
                        "le:",
                        " enabled: true",
                        " world-max-height-excess: 50",
                        " freeze-ticks-in-s: v"),
                getCustomLogger(), "le", "'le'");
    }
}