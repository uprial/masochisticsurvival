package com.gmail.uprial.masochisticsurvival;

import com.gmail.uprial.masochisticsurvival.helpers.TestConfigBase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class MasochisticSurvivalTest extends TestConfigBase {
    @Rule
    public final ExpectedException e = ExpectedException.none();

    @Test
    public void testLoadException() throws Exception {
        e.expect(RuntimeException.class);
        e.expectMessage("[ERROR] Empty percentage of 'nasty-enderman'");
        MasochisticSurvival.loadConfig(null, getPreparedConfig(""), getCustomLogger());
    }
}