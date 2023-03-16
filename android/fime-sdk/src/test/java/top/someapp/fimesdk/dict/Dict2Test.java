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

        File dataFile = new File("../data/pinyin.dat"); // 数据文件
        dataFile.createNewFile();
        RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw");
        // 生成词典文件
        final int codeSize = treeMap.size();
        File dictFile = new File("../data/pinyin.dic");  // 词典文件
        dictFile.createNewFile();
        RandomAccessFile dictRaf = new RandomAccessFile(dictFile, "rw");
        dictRaf.seek(0);
        dictRaf.writeUTF(Strings.simpleFormat("FimeDict:pinyin/v0.2"));
        final long indexOffset = dictRaf.getFilePointer();
        dictRaf.writeInt(2023);
        dataRaf.seek(0);
        Iterator<Map.Entry<String, List<Dict.Item>>> it = treeMap.entrySet()
                                                                 .iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<Dict.Item>> next = it.next();
            dictRaf.writeUTF(next.getKey());
            dictRaf.writeInt((int) dataRaf.getFilePointer());
            for (Dict.Item item : next.getValue()) {
                dataRaf.writeUTF(item.getText());
                dataRaf.writeInt(item.getWeight());
            }
            dataRaf.writeUTF("\n");
            it.remove();
        }
        final long dataOffset = dictRaf.getFilePointer();
        dictRaf.seek(indexOffset);
        dictRaf.writeInt((int) dataOffset);
        dictRaf.seek(dataOffset);
        byte[] buffer = new byte[4 * 1024]; // 4k
        dataRaf.seek(0);
        int len = -1;
        while ((len = dataRaf.read(buffer)) > 0) {
            dictRaf.write(buffer, 0, len);
        }
        dictRaf.close();
        dataRaf.close();
        dataFile.delete();
    }

    @Test
    public void testLoad() throws IOException {
        MapPatriciaTrie<Integer> mapTrie = new MapPatriciaTrie<>();        // 词条树
        File dict = new File("../data/pinyin.dic"); // 词典文件
        RandomAccessFile dictRaf = new RandomAccessFile(dict, "r");
        String idxHead = dictRaf.readUTF();
        assertTrue(idxHead.startsWith("FimeDict:"));
        final int dataOffset = dictRaf.readInt();
        while (dictRaf.getFilePointer() < dataOffset) {
            String code = dictRaf.readUTF();
            int pos = dictRaf.readInt();
            mapTrie.insert(code, pos);
        }
        mapTrie.freeze();

        assertTrue(mapTrie.contains("shi shi"));
        int pos = dataOffset + mapTrie.get("shi shi");
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
