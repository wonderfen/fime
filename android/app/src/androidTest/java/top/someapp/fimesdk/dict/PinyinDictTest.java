package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.utils.Logs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-03-28
 */
public class PinyinDictTest {

    @Test
    public void testBuildPinyin() throws IOException {
        Dict dict = Dict.createPinyinDict("pinyin_dict");
        FimeContext fimeContext = FimeContext.getInstance();
        boolean ok = dict.loadFromCsv(fimeContext.fileInAppHome("pinyin_dict.csv"), null);
        assertTrue(ok);
    }

    @Test
    public void testLoadFromBuild() throws IOException {
        Dict dict = new PinyinDict("pinyin_dict");
        dict.loadFromBuild();
    }

    @Test
    public void testSearch() throws IOException {
        Dict dict = new PinyinDict("pinyin_dict");
        dict.loadFromBuild();
        List<Dict.Item> items = new ArrayList<>();
        for (int i = 'a'; i <= 'z'; i++) {
            if (i == 'v' || i == 'i' || i == 'u') continue;
            dict.search(Character.toString((char) i), items, 100);
            assertTrue(items.size() > 0);
            Logs.i(items.get(0)
                        .toString());
            items.clear();
        }

        dict.search("yi", items, 100);
        assertTrue(items.size() > 0);

        items.clear();
        dict.search("yi ge", items, 10);
        assertTrue(items.size() > 0);

        items.clear();
        dict.search("q", items, 10);
        assertTrue(items.size() > 0);

        items.clear();
        dict.search("yi g", items, 10);
        assertTrue(items.size() > 0);
    }
}
