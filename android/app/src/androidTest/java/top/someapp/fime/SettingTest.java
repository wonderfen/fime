package top.someapp.fime;

import org.junit.Before;
import org.junit.Test;
import top.someapp.fimesdk.Setting;

/**
 * @author zwz
 * Create on 2023-01-15
 */
public class SettingTest {
private Setting setting;

    @Before
    public void setUp() throws Exception {
        setting = Setting.getInstance();
    }

    @Test
    public void testSave() {
        setting.save();
    }
}