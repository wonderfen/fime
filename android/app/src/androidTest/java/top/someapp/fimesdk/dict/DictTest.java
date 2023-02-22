package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-20
 */
public class DictTest {

    @Test
    public void testCompileTo() throws IOException {
        Dict dict = new Dict("pinyin_dict");
        dict.loadFromCsv(FimeContext.getInstance()
                                    .fileInAppHome("pinyin_dict.csv"));
        dict.compileTo(new FileOutputStream(FimeContext.getInstance()
                                                       .fileInCacheDir("pinyin_dict.je")));
    }

    @Test
    public void testLoadFromCompiled() throws IOException {
        Dict dict = Dict.loadFromCompiled(new FileInputStream(FimeContext.getInstance()
                                                                         .fileInCacheDir(
                                                                                 "pinyin_dict.je")));
        List<Dict.Item> items = new ArrayList<>();
        dict.search("yi", items, 100);
        assertTrue(items.size() > 0);
    }
}
