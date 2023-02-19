package top.someapp.fime;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.typesafe.config.Config;
import org.junit.BeforeClass;
import org.junit.Test;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.Schema;
import top.someapp.fimesdk.config.Configs;
import top.someapp.fimesdk.defaults.DefaultSchema;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.view.Keyboards;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author zwz
 * Created on 2023-02-09
 */
public class SchemaTest {

    private static Schema schema;

    @BeforeClass
    public static void setUp() throws Exception {
        File file = FimeContext.getInstance()
                               .fileInAppHome("fime_pinyin_schema.conf");
        schema = new DefaultSchema(/*file*/);
    }

    @Test
    public void testBuild() throws IOException {
        schema.build();
        FimeContext fimeContext = FimeContext.getInstance();
        File s = new File(fimeContext.getCacheDir(), "fime_pinyin_schema.conf/schema.s");
        assertTrue(FileStorage.hasFile(s));

        Config config = Configs.deserialize(new FileInputStream(s));
        assertNotNull(config);
        Schema schema2 = new DefaultSchema();
        schema2.reconfigure(config);

        File k = new File(fimeContext.getCacheDir(), "fime_pinyin_schema.conf/keyboards.s");
        assertTrue(FileStorage.hasFile(k));
        config = Configs.deserialize(new FileInputStream(k));
        assertNotNull(config);
        new Keyboards(config);
    }
}
