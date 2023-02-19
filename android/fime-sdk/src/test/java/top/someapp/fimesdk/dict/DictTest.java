package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-04
 */
public class DictTest {

    private static final String dictSource = "../data/pinyin_dict.csv";
    private static final String dictOut = "../data/pinyin.dict";
    private static final File dictOutFile = new File(dictOut);

    @Test
    public void testLoadCsvAndCompile() throws IOException {
        Dict dict = new Dict("pinyin_dict");
        dict.loadFromCsv(new File(dictSource));
        dict.compileTo(new FileOutputStream(dictOutFile));
    }

    @Test
    public void testCompileThenLoad() throws IOException {
        Dict dict = new Dict("test");
        dict.loadFromCsv(new File(dictSource));
        dict.compileTo(new FileOutputStream(dictOutFile));
        Dict.loadFromCompiled(new FileInputStream(dictOutFile));
        assertNotNull(dict);
        assertTrue(dict.getSize() > 0);
        System.out.println(dict);
    }

    @Test
    public void testSearch() throws IOException {
        Dict dict = Dict.loadFromCompiled(new FileInputStream(dictOutFile));
        String pinyin = "yi ge ren";
        System.out.println(" === find in progress.");
        for (int i = 1; i <= pinyin.length(); i++) {
            String key = pinyin.substring(0, i);
            System.out.println(key + ": ===");
            List<Dict.Item> result = new ArrayList<>();
            if (dict.search(key, result, 3)) {
                for (Dict.Item item : result) {
                    System.out.println(item);
                }
            }
            else {
                System.out.println("Empty !!");
            }
        }
    }

    @Test
    public void testLoadFromCompiled() throws IOException {
        Dict dict = Dict.loadFromCompiled(new FileInputStream(dictOutFile));
        assertNotNull(dict);
        assertTrue(dict.getSize() > 0);
        System.out.println(dict);
    }
}
