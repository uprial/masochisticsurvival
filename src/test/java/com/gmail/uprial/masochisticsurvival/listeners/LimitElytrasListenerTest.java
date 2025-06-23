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
                        " initial-height-excess: 10",
                        " height-per-freeze-second: 3"),
                getParanoiacCustomLogger(), "le", "'le'");
        assertNotNull(listener);
        assertEquals("{initial-height-excess: 10, height-per-freeze-second: 3}",
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
        e.expectMessage("A initial height excess of 'le' is not an integer");
        getFromConfig(getPreparedConfig(
                        "le:",
                        " enabled: true",
                        " initial-height-excess: v"),
                getCustomLogger(), "le", "'le'");
    }

    @Test
    public void testFreezeTicksInS() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("A height per freeze second of 'le' is not an integer");
        getFromConfig(getPreparedConfig(
                        "le:",
                        " enabled: true",
                        " initial-height-excess: 10",
                        " height-per-freeze-second: v"),
                getCustomLogger(), "le", "'le'");
    }
}