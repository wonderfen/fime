/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.osfans.trime.core.Rime;
import org.junit.Before;
import org.junit.Test;
import top.someapp.fimesdk.utils.Logs;

/**
 * @author zwz
 * Created on 2023-06-21
 */
public class RimeTest {

    private Rime rime;

    @Before
    public void setUp() throws Exception {
        rime = Rime.get(FimeContext.getInstance()
                                   .getContext(), true);
        assertNotNull(rime);
    }

    @Test
    public void testBuildAndDeploy() {
        String appHome = FimeContext.getInstance()
                                    .getAppHomeDir()
                                    .getAbsolutePath();
        // Rime.deployer_initialize(appHome, appHome);
        // Rime.deploy_schema("pinyin_simp");
        Rime.deploy();
        Rime.prebuild();
    }

    @Test
    public void testSchema() {
        // int session = Rime.create_session();
        assertTrue(Rime.select_schema("pinyin_simp"));
        // Rime.setOption("ascii_mode", false);
        if (Rime.isAsciiMode()) {
            Rime.toggleOption("ascii_mode");
        }
        Rime.onKey(new int[] { 97, 0 });    // a ?
        Rime.RimeCandidate[] candidates = Rime.getCandidates();
        if (candidates != null && candidates.length > 0) {
            for (Rime.RimeCandidate c : candidates) {
                Logs.i(c.text);
            }
        }
        Rime.destroy();
    }
}
