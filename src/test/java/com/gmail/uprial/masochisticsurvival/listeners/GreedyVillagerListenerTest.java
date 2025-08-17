package com.gmail.uprial.masochisticsurvival.listeners;

import com.gmail.uprial.masochisticsurvival.config.InvalidConfigException;
import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static com.gmail.uprial.masochisticsurvival.listeners.GreedyVillagerListener.getFromConfig;
import static org.junit.Assert.*;

public class GreedyVillagerListenerTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testWhole() throws Exception {
        GreedyVillagerListener listener = getFromConfig(getPreparedConfig(
                        "gv:",
                        "  replace-protection: true",
                        "  overprice-mending: true",
                        "  info-log-about-actions: true"),
                getParanoiacCustomLogger(), "gv", "'gv'");
        assertNotNull(listener);
        assertEquals("{replace-protection: true, overprice-mending: true, info-log-about-actions: true}",
                listener.toString());
    }

    @Test
    public void testNull() throws Exception {
        GreedyVillagerListener listener = getFromConfig(getPreparedConfig(
                        "gv:",
                        " replace-protection: false",
                        " overprice-mending: false"),
                getDebugFearingCustomLogger(), "gv", "'gv'");
        assertNull(listener);
    }

    @Test
    public void testEmpty() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Empty 'replace protection' flag of 'gv'");
        getFromConfig(getPreparedConfig(
                        "?:"),
                getCustomLogger(), "gv", "'gv'");
    }

    @Test
    public void testWrongPercentage() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Invalid 'replace protection' flag of 'gv'");
        getFromConfig(getPreparedConfig(
                        "gv:",
                        " replace-protection: v"),
                getCustomLogger(), "gv", "'gv'");
    }

    @Test
    public void testWrongPower() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Invalid 'overprice mending' flag of 'gv'");
        getFromConfig(getPreparedConfig(
                        "gv:",
                        " replace-protection: true",
                        " overprice-mending: v"),
                getCustomLogger(), "gv", "'gv'");
    }

    @Test
    public void testWrongInfoLogAboutActions() throws Exception {
        e.expect(InvalidConfigException.class);
        e.expectMessage("Invalid 'info-log-about-actions' flag of 'gv'");
        getFromConfig(getPreparedConfig(
                        "gv:",
                        " replace-protection: true",
                        " overprice-mending: true",
                        " info-log-about-actions: v"),
                getCustomLogger(), "gv", "'gv'");
    }
}