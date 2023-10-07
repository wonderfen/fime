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

import java.util.List;

/**
 * @author zwz
 * Created on 2023-06-21
 */
public class RimeTest {

    private Rime rime;

    @Before
    public void setUp() throws Exception {
        rime = Rime.get(FimeContext.getInstance()
                                   .getContext(), false);
        assertNotNull(rime);
    }

    @Test
    public void testBuildAndDeploy() {
        Rime.destroy();
        // installation.yaml
        // build/default.yaml
        // build/xxx.schema.yaml
        // build/xxx.bin
        // xxx.userdb/
        Rime.get(FimeContext.getInstance()
                            .getContext(), true);   // full_check = true 会触发 部署
        Rime.check(true);
    }

    @Test
    public void testDeploySchema() {
        SchemaManager.buildAll(() -> {Logs.i("Deploy success.");},
                               () -> {Logs.i("Deploy failed!");});
    }

    @Test
    public void testGetSchemaList() {
        List schemaList = Rime.get_schema_list();
        for (Object o : schemaList) {
            Logs.i(o.toString());
        }
    }

    @Test
    public void testSchema() {
        assertTrue(Rime.select_schema("fime_pinyin"));
        Rime.onKey(new int[] { 'a', 0 });    // a ?, 0 表示没有按下任何修饰键(modifier)
        Rime.RimeCandidate[] candidates = Rime.getCandidates();
        if (candidates != null && candidates.length > 0) {
            for (Rime.RimeCandidate c : candidates) {
                Logs.i(c.text);
            }
        }
        Rime.clearComposition();
        Rime.simulate_key_sequence("zhon");
        Rime.onKey(new int[] { 'g', Rime.META_RELEASE_ON });    // Rime.META_RELEASE_ON 也可以换成 0
        candidates = Rime.getCandidates();
        if (candidates != null && candidates.length > 0) {
            for (Rime.RimeCandidate c : candidates) {
                Logs.i(c.text);
            }
        }
        Rime.destroy();
    }

    @Test
    public void testSearch() {
        String code = "xianshishanxideshenghui";
        search(code);
    }

    @Test
    public void testLuaTranslator() {
        search("date");
    }

    private void search(String code) {
        for (int i = 0; i < code.length(); i++) {
            Rime.onKey(new int[] { code.charAt(i), 0 });
            Rime.RimeCandidate[] candidates = Rime.getCandidatesWithoutSwitch();
            for (Rime.RimeCandidate c : candidates) {
                Logs.i("%s:%s", code.substring(0, i + 1), c.text);
            }
        }
    }
}
