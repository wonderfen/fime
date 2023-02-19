package top.someapp.fime;

import static org.junit.Assert.assertFalse;

import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

import java.io.File;

/**
 * @author zwz
 * Create on 2023-01-08
 */
public class FimeContextTest {

    @Test
    public void testGetAppHomeDir() {
        FimeContext fimeContext = FimeContext.getInstance();
        try {
            Thread.sleep(3000);
        }
        catch (InterruptedException ignored) {
        }
        File appHomeDir = fimeContext.getAppHomeDir();
        assertFalse(appHomeDir == null || !appHomeDir.exists());
    }
}
