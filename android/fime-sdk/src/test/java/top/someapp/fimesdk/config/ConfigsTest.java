package top.someapp.fimesdk.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import org.junit.Test;

import java.io.File;

/**
 * @author zwz
 * Created on 2023-03-08
 */
public class ConfigsTest {

    @Test
    public void testLoad() {
        Config load = Configs.load(new File("../data/default.conf"), true);
        assertTrue(load.hasPath("theme"));

        Config theme = load.getConfig("theme");
        assertTrue(theme.hasPath("light"));
        assertTrue(theme.hasPath("dark"));
        assertTrue(theme.hasPath("customer"));

        assertEquals("light", theme.getString("light"));
        assertEquals("dark-override", theme.getString("dark"));
        assertEquals("customer", theme.getString("customer"));
    }
}
