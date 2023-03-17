package top.someapp.fimesdk.dict;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import it.unimi.dsi.fastutil.objects.Object2ObjectRBTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.patricia.MapPatriciaTrieNode;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 字典
 *
 * @author zwz
 * Created on 2023-02-04
 */
@Keep
public class Dict implements Comparator<Dict.Item> {

    public static final String SUFFIX = ".dic"; // 生成词典的后缀名
    private static final short kVersion = 2;       // 版本号
    private static H2 h2;   // 用户词词典
    private final String name;  // 词典名
    private final MapPatriciaTrie<Integer> mapTrie;        // 词条树
    private int size;           // 包含的词条数
    private boolean sealed;     // 词典是否构建完成
    private transient Map<String, List<Item>> itemMap;  // code -> Item[]
    private RandomAccessFile raf;
    private long dataOffset;

    public Dict(String name) {
        this.name = name;
        mapTrie = new MapPatriciaTrie<>();
    }

    public static Dict loadFromCompiled(File file) throws IOException {
        RandomAccessFile dictRaf = new RandomAccessFile(file, "r");
        String idxHead = dictRaf.readUTF();
        Dict dict;
        if (idxHead.startsWith("FimeDict:")) {
            dict = new Dict(idxHead.substring(9));
        }
        else {
            throw new IOException("Unknown file format!");
        }
        short version = dictRaf.readShort();
        if (version < 1 || version > kVersion) {
            throw new IOException("Not supported version: " + version + "!");
        }

        final int dataOffset = dictRaf.readInt();
        while (dictRaf.getFilePointer() < dataOffset) {
            String code = dictRaf.readUTF();
            int pos = dictRaf.readInt();
            dict.mapTrie.insert(code, pos);
        }
        dict.mapTrie.trimToSize();
        dict.mapTrie.freeze();
        return dict;
    }

    static int compareItems(Item o1, Item o2) {
        String code1 = o1.getCode();
        String code2 = o2.getCode();
        if (code1.equals(code2)) { // 编码相同时，weight 优先
            if (o1.getWeight() >= o2.getWeight()) return -1;
            return o2.getWeight() - o1.getWeight();
        }
        if (o1.getLength() == o2.getLength()) { // 编码不同，词长相同， weight 优先
            if (o1.getWeight() >= o2.getWeight()) return -1;
            return o2.getWeight() - o1.getWeight();
        }
        // 编码不同，词长不同，编码短的优先
        return code1.length() - code2.length();
    }

    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public boolean loadFromCsv(File csvFile) throws IOException {
        return loadFromCsv(new FileInputStream(csvFile), new Converter());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean loadFromCsv(File csvFile, @NonNull Converter converter) throws IOException {
        return loadFromCsv(new FileInputStream(csvFile), converter);
    }

    public boolean loadFromCsv(InputStream ins, @NonNull Converter converter) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            String[] parts = line.split("\t");
            String text = parts[0];
            String code = converter.convert(parts[1]);
            if (parts.length > 2 && !Strings.isNullOrEmpty(parts[2])) {
                put(new Item(text, code, Integer.decode(parts[2])));
            }
            else {
                put(new Item(text, code));
            }
        }
        ins.close();
        return build();
    }

    public boolean search(@NonNull String prefix, @NonNull List<Item> result, int limit) {
        return search(prefix, -1, result, limit);
    }

    public boolean search(@NonNull String prefix, final int wordLength, @NonNull List<Item> result,
            int limit) {
        return search(prefix, wordLength, result, limit, this);
    }

    public boolean search(@NonNull String prefix, final int wordLength, @NonNull List<Item> result,
            int limit, Comparator<Item> comparator) {
        ObjectArrayPriorityQueue<Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                              comparator);
        // 优先查询用户词
        initH2();
        initDictFile();
        List<Item> userItems = h2.query(prefix, limit);
        if (mapTrie.contains(prefix)) { // 全部匹配
            MapPatriciaTrieNode<Integer> node = mapTrie.getNode(prefix);
            final int index = node.getValue();
            try {
                raf.seek(dataOffset + index);
                for (int i = 0; raf.getFilePointer() < raf.length() && i < limit; i++) {
                    String text = raf.readUTF();
                    if (text.equals("\n")) break;
                    int weight = raf.readInt();
                    queue.enqueue(new Item(text, prefix, weight));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {    // 前缀预测匹配
            for (Map.Entry<String, Integer> entry : mapTrie.predictiveSearchEntries(
                    prefix)) {
                final int index = entry.getValue();
                try {
                    raf.seek(dataOffset + index);
                    for (int i = 0; raf.getFilePointer() < raf.length() && i < limit; i++) {
                        String text = raf.readUTF();
                        if ("\n".equals(text)) break;
                        int weight = raf.readInt();
                        if (wordLength < 0 || text.length() == wordLength) {
                            queue.enqueue(new Item(text, entry.getKey(), weight));
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    break;  // break 可能更合适一点
                }
            }
        }
        boolean hit = false;
        int count = 0;
        Iterator<Item> it = userItems.iterator();
        while (it.hasNext() && count < limit) {
            if (!hit) hit = true;
            result.add(it.next());
            it.remove();
            count++;
        }
        while (!queue.isEmpty() && count < limit) {
            if (!hit) hit = true;
            result.add(queue.dequeue());
            count++;
        }
        queue.clear();
        return hit;
    }

    public boolean searchPrefix(@NonNull String prefix, final int extendCodeLength,
            @NonNull List<Item> result, int limit,
            Comparator<Item> comparator) {
        ObjectArrayPriorityQueue<Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                              comparator);
        // 前缀预测匹配
        initDictFile();
        final int maxCodeLength = prefix.length() + extendCodeLength;
        for (Map.Entry<String, Integer> entry : mapTrie.predictiveSearchEntries(
                prefix)) {
            final int index = entry.getValue();
            try {
                raf.seek(dataOffset + index);
                for (int i = 0; raf.getFilePointer() < raf.length() && i < limit; i++) {
                    String text = raf.readUTF();
                    if ("\n".equals(text)) break;
                    int weight = raf.readInt();
                    if (entry.getKey()
                             .length() <= maxCodeLength) {
                        queue.enqueue(new Item(text, entry.getKey(), weight));
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        boolean hit = false;
        int count = 0;
        while (!queue.isEmpty() && count < limit) {
            if (!hit) hit = true;
            result.add(queue.dequeue());
            count++;
        }
        queue.clear();
        return hit;
    }

    public void recordUserWord(Item item) {
        initH2();
        h2.insertOrUpdate(item);
    }

    public void close() {
        if (h2 != null) h2.stop();
        if (raf != null) {
            try {
                raf.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            raf = null;
        }
    }

    @NonNull @Override public String toString() {
        return "Dict{"
                + name +
                ", size=" + size +
                ", sealed=" + sealed +
                '}';
    }

    @Override public int compare(Item o1, Item o2) {
        return Dict.compareItems(o1, o2);
    }

    @SuppressWarnings("all")
    protected Dict put(@NonNull Item item) {
        if (sealed) return this;
        if (itemMap == null) itemMap = new Object2ObjectRBTreeMap<>();
        if (!itemMap.containsKey(item.getCode())) {
            itemMap.put(item.getCode(), new ArrayList<>());
        }
        itemMap.get(item.getCode())
               .add(item);
        size++;
        return this;
    }

    private void initH2() {
        if (h2 == null) {
            h2 = new H2(name);
            h2.start();
            return;
        }
        if (!name.equals(h2.getId())) {
            h2.stop();
            h2 = new H2(name);
            h2.start();
        }
    }

    private void initDictFile() {
        if (raf == null) {
            try {
                raf = new RandomAccessFile(FimeContext.getInstance()
                                                      .fileInCacheDir(name + SUFFIX), "r");
                raf.readUTF();  // head
                raf.readShort(); // version
                dataOffset = raf.readInt();  // dataOffset
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored") private boolean build() throws IOException {
        if (sealed) return false;

        Iterator<Map.Entry<String, List<Item>>> it = itemMap.entrySet()
                                                            .iterator();
        File dataFile = FimeContext.getInstance()
                                   .fileInCacheDir(name + ".dat"); // 数据文件
        dataFile.createNewFile();
        RandomAccessFile dataRaf = new RandomAccessFile(dataFile, "rw");
        // 生成词典文件
        File dictFile = FimeContext.getInstance()
                                   .fileInCacheDir(name + SUFFIX);  // 词典文件
        dictFile.createNewFile();
        RandomAccessFile dictRaf = new RandomAccessFile(dictFile, "rw");
        dictRaf.seek(0);
        dictRaf.writeUTF(Strings.simpleFormat("FimeDict:%s", getName()));
        dictRaf.writeShort(kVersion);
        final long indexOffset = dictRaf.getFilePointer();
        dictRaf.writeInt(2023);
        dataRaf.seek(0);
        size = 0;
        while (it.hasNext()) {
            Map.Entry<String, List<Item>> next = it.next();
            dictRaf.writeUTF(next.getKey());
            dictRaf.writeInt((int) dataRaf.getFilePointer());
            mapTrie.insert(next.getKey(), (int) dataRaf.getFilePointer());
            List<Item> values = next.getValue();
            for (Item item : values) {
                dataRaf.writeUTF(item.getText());
                dataRaf.writeInt(item.getWeight());
            }
            dataRaf.writeUTF("\n");
            size += values.size();
            it.remove();
        }
        mapTrie.trimToSize();
        mapTrie.freeze();
        itemMap = null;
        sealed = true;

        final long dataOffset = dictRaf.getFilePointer();
        dictRaf.seek(indexOffset);
        dictRaf.writeInt((int) dataOffset);
        dictRaf.seek(dataOffset);
        byte[] buffer = new byte[4 * 1024]; // 4k
        dataRaf.seek(0);
        int len;
        while ((len = dataRaf.read(buffer)) > 0) {
            dictRaf.write(buffer, 0, len);
        }
        dictRaf.close();
        dataRaf.close();
        dataFile.delete();
        return true;
    }

    @Keep
    public static class Item implements Serializable, Comparable<Item> {

        private /*final*/ String text;  // for Serializable
        private /*final*/ String code;  // for Serializable
        private int weight;

        @Keep
        public Item() { // for Serializable
        }

        public Item(String text, String code) {
            this(text, code, 0);
        }

        public Item(String text, String code, int weight) {
            this.text = text;
            this.code = code;
            this.weight = weight;
        }

        public String getText() {
            return text;
        }

        public String getCode() {
            return code;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        public int getLength() {
            return getText().length();
        }

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Item item = (Item) o;
            return Objects.equals(code, item.code) && Objects.equals(text, item.text);
        }

        @Override public int hashCode() {
            return Objects.hash(code, text);
        }

        @NonNull @Override public String toString() {
            return "Item{" +
                    code +
                    ", " + text +
                    ", weight=" + weight +
                    '}';
        }

        @Override public int compareTo(Item o) {
            return Dict.compareItems(this, o);
        }
    }
}
