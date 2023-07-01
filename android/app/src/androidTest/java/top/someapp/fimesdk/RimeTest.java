/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk;

import static org.junit.Assert.assertNotNull;

import com.osfans.trime.core.Rime;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zwz
 * Created on 2023-06-21
 */
public class RimeTest {

    private Rime rime;

    @Before
    public void setUp() throws Exception {
        rime = Rime.get(FimeContext.getInstance()
                            .getContext());
    }

    @Test
    public void testDryRun() {
        assertNotNull(rime);
    }
}
