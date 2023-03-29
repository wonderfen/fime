package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.utils.Logs;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-20
 */
public class DictTest {

    @Before
    public void setUp() throws Exception {
        Dict dict = new Dict("pinyin_dict", ' ');
        dict.loadFromCsv(FimeContext.getInstance()
                                    .fileInAppHome("pinyin_dict.csv"));
    }

    @Test
    public void testLoadFromCompiled() throws IOException {
        Dict dict = new Dict("pinyin_dict", ' ');
        dict.loadFromBuild();
        List<Dict.Item> items = new ArrayList<>();
        dict.search("yi", items, 100);
        assertTrue(items.size() > 0);

        items.clear();
        dict.search("q", items, 10);
        assertTrue(items.size() > 0);
    }

    @Test
    public void testByteBuffer() throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(100);
        OutputStream out = new OutputStream() {
            @Override public void write(int b) {
                System.out.println("write byte:" + b);
            }
        };
        for (int i = 0; i < 100; i++) {
            System.out.println("pos:" + buffer.position());
            buffer.put((byte) i);
            System.out.println("pos:" + buffer.position());
        }
        out.write(buffer.array());
        System.out.println("remaining 1:" + buffer.remaining());
        buffer.clear();
        System.out.println("remaining 2:" + buffer.remaining());
    }

    @Test
    public void testTableSearch() throws IOException {
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

    @Test
    public void testPinyinSearch() throws IOException {
        Dict dict = new Dict("pinyin_dict", ' ');
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
