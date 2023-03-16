package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.trie4j.patricia.MapPatriciaTrie;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zwz
 * Created on 2023-03-16
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class Dict2Test {

    private TreeMap<String, List<Dict.Item>> treeMap = new TreeMap<>();

    @Test @SuppressWarnings("all")
    public void testBuild() throws IOException {
        File csv = new File("../data/pinyin_dict.csv");
        BufferedReader reader = new BufferedReader(new FileReader(csv));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            String[] segments = line.split("\t");
            Dict.Item item;
            if (segments.length == 3) {
                item = new Dict.Item(segments[0], segments[1], Integer.decode(segments[2]));
            }
            else {
                item = new Dict.Item(segments[0], segments[1]);
            }
            if (!treeMap.containsKey(item.getCode())) {
                treeMap.put(item.getCode(), new ArrayList<>());
            }
            treeMap.get(item.getCode())
                   .add(item);
        }
        reader.close();

        File dict = new File("../data/pinyin.dic"); // 词典文件
        dict.createNewFile();
        RandomAccessFile dictRaf = new RandomAccessFile(dict, "rw");
        // 生成词典文件
        final int codeSize = treeMap.size();
        File idx = new File("../data/pinyin.idx");  // 索引文件
        idx.createNewFile();
        RandomAccessFile idxRaf = new RandomAccessFile(idx, "rw");
        idxRaf.seek(0);
        idxRaf.writeUTF(Strings.simpleFormat("FimeDictIdx:pinyin/codes:%d\n", codeSize));
        dictRaf.seek(0);
        dictRaf.writeUTF(Strings.simpleFormat("FimeDict:pinyin/codes:%d\n", codeSize));
        Iterator<Map.Entry<String, List<Dict.Item>>> it = treeMap.entrySet()
                                                                 .iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Dict.Item>> next = it.next();
            idxRaf.writeUTF(next.getKey());
            idxRaf.writeInt((int) dictRaf.getFilePointer());
            // mapTrie.put(next.getKey(), (int) dictRaf.getFilePointer());
            for (Dict.Item item : next.getValue()) {
                dictRaf.writeUTF(item.getText());
                dictRaf.writeInt(item.getWeight());
            }
            dictRaf.writeUTF("\n");
            it.remove();
        }
        dictRaf.close();
        idxRaf.close();
    }

    @Test
    public void testLoad() throws IOException {
        MapPatriciaTrie<Integer> mapTrie = new MapPatriciaTrie<>();        // 词条树
        File idx = new File("../data/pinyin.idx");  // 索引文件
        File dict = new File("../data/pinyin.dic"); // 词典文件
        RandomAccessFile idxRaf = new RandomAccessFile(idx, "r");
        RandomAccessFile dictRaf = new RandomAccessFile(dict, "r");
        String idxHead = idxRaf.readUTF();
        assertTrue(idxHead.startsWith("FimeDictIdx:"));
        while (idxRaf.getFilePointer() < idxRaf.length()) {
            String code = idxRaf.readUTF();
            int pos = idxRaf.readInt();
            mapTrie.insert(code, pos);
        }
        idxRaf.close();
        mapTrie.freeze();

        assertTrue(mapTrie.contains("shi shi"));
        Integer pos = mapTrie.get("shi shi");
        dictRaf.seek(pos);
        List<String> texts = new ArrayList<>();
        while (true) {
            String text = dictRaf.readUTF();
            if (text.equals("\n")) break;
            int weight = dictRaf.readInt();
            assertTrue(weight >= 0);
            texts.add(text);
        }
        assertTrue(texts.contains("试试"));
        dictRaf.close();
    }
}
