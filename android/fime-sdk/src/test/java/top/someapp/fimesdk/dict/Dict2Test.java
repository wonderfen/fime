package top.someapp.fimesdk.dict;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.trie4j.patricia.MapPatriciaTrie;
import top.someapp.fimesdk.utils.Serializes;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zwz
 * Created on 2023-03-16
 */
public class Dict2Test {

    private final TreeMap<String, List<Dict.Item>> treeMap = new TreeMap<>();

    @Test
    public void testBuild2() throws IOException {
        loadToTreeMap();
        File file = new File("../data/pinyin_dict.dic");
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.writeUTF(Strings.simpleFormat("FimeDict:%s\n", "pinyin"));
        final int keySize = treeMap.size();
        raf.writeShort(2);  // version
        raf.writeInt(keySize);   // keySize
        final long metaOffset = raf.getFilePointer();
        raf.writeLong(0L);  // placeholder to save tire offset

        StringBuilder content = new StringBuilder();
        Iterator<Map.Entry<String, List<Dict.Item>>> it = treeMap.entrySet()
                                                                 .iterator();
        MapPatriciaTrie<Long> mapTrie = new MapPatriciaTrie<>();
        // long keyOffset = metaOffset;
        while (it.hasNext()) {
            Map.Entry<String, List<Dict.Item>> next = it.next();
            long start = raf.getFilePointer();
            for (Dict.Item item : next.getValue()) {
                content.append(item.getText())
                       .append("\t")
                       .append(item.getWeight())
                       .append("\n");
            }
            raf.write(content.toString()
                             .getBytes(StandardCharsets.UTF_8));
            content.setLength(0);
            mapTrie.insert(next.getKey(), start << 32 | raf.getFilePointer());
            it.remove();
        }

        mapTrie.trimToSize();
        mapTrie.freeze();
        long pos = raf.getFilePointer();
        raf.seek(metaOffset);
        raf.writeLong(pos);
        raf.seek(pos);
        OutputStream out = new OutputStream() {
            @Override public void write(int b) throws IOException {
                raf.write(b);
            }
        };
        Serializes.serialize(mapTrie, out);
        raf.close();
    }

    @Test @SuppressWarnings("unused")
    public void testLoad2() throws IOException {

        File dictFile = new File("../data/pinyin_dict.dic");
        RandomAccessFile raf = new RandomAccessFile(dictFile, "r");
        String head = raf.readUTF();
        short version = raf.readShort();
        int keySize = raf.readInt();
        long trieOffset = raf.readLong();
        raf.seek(trieOffset);
        byte[] buffer = new byte[(int) (raf.length() - trieOffset)];
        int len = raf.read(buffer);
        MapPatriciaTrie<Integer> mapTrie = Serializes.deserialize(
                new ByteArrayInputStream(buffer, 0, len));
        assertTrue(mapTrie.size() > 0);
    }

    @SuppressWarnings("all")
    private void loadToTreeMap() throws IOException {
        treeMap.clear();
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
    }
}