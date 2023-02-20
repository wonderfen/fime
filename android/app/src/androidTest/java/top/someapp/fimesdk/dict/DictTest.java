package top.someapp.fimesdk.dict;

import org.junit.BeforeClass;
import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author zwz
 * Created on 2023-02-20
 */
public class DictTest {

    private static Dict dict;

    @BeforeClass
    public static void beforeClass() throws Exception {
        dict = new Dict("pinyin_dict_big");
    }

    @Test
    public void testLoadFromCsv() throws IOException {
        dict.loadFromCsv(FimeContext.getInstance()
                                    .fileInAppHome("pinyin_big_dict.csv"));
        File target = FimeContext.getInstance()
                                 .fileInCacheDir("pinyin_big_dict.csv.s");
        dict.compileTo(new FileOutputStream(target));
    }
}
