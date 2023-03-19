package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import top.someapp.fimesdk.FimeContext;

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
    }

    @Test
    public void testLoadFromCompiled() throws IOException {
        Dict dict = new Dict("pinyin_dict");
        dict.loadFromBuild();
        List<Dict.Item> items = new ArrayList<>();
        dict.search("yi", items, 100);
        assertTrue(items.size() > 0);

        items.clear();
        dict.search("q", items, 10);
        assertTrue(items.size() > 0);
    }

    @Test
    public void testSearch() throws IOException {
        FimeContext fimeContext = FimeContext.getInstance();
        Dict dict = new Dict("wubi86_dict");
        dict.loadFromCsv(fimeContext.fileInAppHome("wubi86_dict.csv"));

        List<Dict.Item> items = new ArrayList<>();
        dict.search("g", items, 2);

        assertEquals(2, items.size());
        assertEquals("一", items.get(0)
                               .getText());
        assertEquals("王", items.get(1)
                               .getText());

        items.clear();
        dict.search("z", items, 2);
        assertTrue(items.isEmpty());
    }
}
