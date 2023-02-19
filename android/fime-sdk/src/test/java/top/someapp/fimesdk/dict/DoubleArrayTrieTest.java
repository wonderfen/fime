package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import com.hankcs.algorithm.AhoCorasickDoubleArrayTrie;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zwz
 * Created on 2023-02-16
 */
public class DoubleArrayTrieTest {

    private static final String kDictFile = "../data/stroke5_dict.csv";
    private static DoubleArrayTrie dart;

    @BeforeClass
    public static void beforeClass() throws Exception {
        dart = new DoubleArrayTrie();
    }

    @Test
    public void testBuild() {
        File dictFile = new File(kDictFile);
        List<Dict.Item> items = new ArrayList<>(65535);
        List<String> keys = new ArrayList<>(65535);
        try (BufferedReader reader = new BufferedReader(new FileReader(dictFile))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] segments = line.split("\t", 3);
                Dict.Item item = new Dict.Item(segments[0], segments[1],
                                               Integer.decode(segments[2]));
                items.add(item);
                keys.add(item.getCode());
            }
            int[] length = new int[items.size()];
            int[] values = new int[items.size()];
            for (int i = 0; i < items.size(); i++) {
                Dict.Item item = items.get(i);
                length[i] = item.getCode()
                                .length();
                values[i] = i;
            }
            int ok = dart.build(keys, length, values, keys.size());
            assertEquals(0, ok);
            dart.save("../data/stroke5_dict.dat");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testReadThenSearch() throws IOException {
        dart.open("../data/stroke5_dict.dat");
        List<Integer> ids = dart.commonPrefixSearch("1234");
        assertFalse(ids == null || ids.isEmpty());
        System.out.println("commonPrefixSearch=" + ids);
        int i = dart.exactMatchSearch("1234");
        System.out.println("exactMatchSearch: " + i);
    }

    @Test
    public void testAhoCorasickDoubleArrayTrie() {
        AhoCorasickDoubleArrayTrie<List<Dict.Item>> trie = new AhoCorasickDoubleArrayTrie();
        Map<String, List<Dict.Item>> map = new TreeMap<>();
        File dictFile = new File(kDictFile);
        try (BufferedReader reader = new BufferedReader(new FileReader(dictFile))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] segments = line.split("\t", 3);
                Dict.Item item = new Dict.Item(segments[0], segments[1],
                                               Integer.decode(segments[2]));
                if (!map.containsKey(item.getCode())) {
                    map.put(item.getCode(), new ArrayList<>());
                }
                map.get(item.getCode())
                   .add(item);
            }
            trie.build(map);
            // trie.save(new ObjectOutputStream(
            //         new FileOutputStream(new File("../data/stroke5_dict.trie"))));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (AhoCorasickDoubleArrayTrie.Hit<List<Dict.Item>> hit : trie.parseText("1234")) {
            for (Dict.Item item : hit.value) {
                System.out.println(item);
            }
        }
        System.out.println(" === ");
        for (Dict.Item item : trie.findFirst("1234").value) {
            System.out.println(item);
        }
    }
}
