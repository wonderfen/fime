package top.someapp.fimesdk.dict;

import androidx.annotation.NonNull;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import org.trie4j.patricia.MapPatriciaTrie;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * 使用 RAF 读写的词典
 *
 * @author zwz
 * Created on 2023-03-18
 */
class DictRaf implements Comparator<Dict.Item> {

    public static final String SUFFIX = ".dic"; // 生成词典的后缀名
    private static final String kTrie = ".trie";
    private static final short kVersion = 2;       // 版本号
    private static H2 h2;   // 用户词词典
    private static String recentUse;
    private final String name;  // 词典名
    private final MapPatriciaTrie<Long> mapTrie;        // 词条树
    private boolean sealed;     // 词典是否构建完成
    private transient Map<String, List<Dict.Item>> itemMap;  // code -> Item[]
    private RandomAccessFile raf;

    public DictRaf(String name) {
        this.name = name;
        this.mapTrie = new MapPatriciaTrie<>();
    }

    static int compareItems(Dict.Item o1, Dict.Item o2) {
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

    public boolean loadFromBuild() {
        FimeContext fimeContext = FimeContext.getInstance();
        File file = fimeContext.fileInCacheDir(name + Dict.SUFFIX);
        if (!file.exists()) return false;

        Logs.d("loadFromBuild start.");
        try {
            raf = new RandomAccessFile(file, "r");
            String head = raf.readUTF();
            if (!head.startsWith("FimeDict:")) {
                throw new IOException("Unknown file format!");
            }
            short version = raf.readShort();    // version
            if (version < 0 || version > kVersion) {
                throw new IOException("Not supported version: " + version);
            }

            final int keySize = raf.readInt();
            for (int i = 0; i < keySize; i++) {
                String key = raf.readUTF();
                long range = raf.readLong();
                mapTrie.insert(key, range);
            }
            mapTrie.trimToSize();
            mapTrie.freeze();
        }
        catch (IOException e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
        sealed = true;
        Logs.d("loadFromBuild end.");
        return true;
    }

    public boolean loadFromCsv(File csvFile) throws IOException {
        return loadFromCsv(csvFile, new Converter());
    }

    public boolean loadFromCsv(File csvFile, @NonNull Converter converter) throws IOException {
        if (sealed) return false;

        readItems(new BufferedReader(new FileReader(csvFile)), converter);
        build();
        return true;
    }

    public boolean search(@NonNull String prefix, @NonNull List<Dict.Item> result, int limit) {
        return search(prefix, -1, result, limit);
    }

    public boolean search(@NonNull String prefix, final int wordLength,
            @NonNull List<Dict.Item> result,
            int limit) {
        return search(prefix, wordLength, result, limit, this);
    }

    public boolean search(@NonNull String prefix, final int wordLength,
            @NonNull List<Dict.Item> result,
            int limit, Comparator<Dict.Item> comparator) {
        ObjectArrayPriorityQueue<Dict.Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                                   comparator);
        // 优先查询用户词
        initH2();
        initDictFile();
        List<Dict.Item> userItems = h2.queryUserItems(prefix, limit);
        if (mapTrie.contains(prefix)) { // 全部匹配
            loadItems(queue, mapTrie.get(prefix), prefix);
        }
        else {    // 前缀预测匹配
            for (Map.Entry<String, Long> entry : mapTrie.predictiveSearchEntries(
                    prefix)) {
                long range = entry.getValue();
                final int start = (int) (range >> 32);
                final int end = (int) range;
                try {
                    raf.seek(start);
                    byte[] buffer = new byte[end - start];
                    raf.read(buffer);
                    String content = new String(buffer, StandardCharsets.UTF_8);
                    for (String seg : content.split("[\n]")) {
                        int index = seg.indexOf('\t');
                        if (wordLength < 0 || index <= wordLength) {
                            queue.enqueue(
                                    new Dict.Item(seg.substring(0, index), prefix,
                                                  Integer.decode(seg.substring(index + 1))));
                        }
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                if (queue.size() >= limit) break;
            }
        }
        boolean hit = false;
        int count = 0;
        Iterator<Dict.Item> it = userItems.iterator();
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
            @NonNull List<Dict.Item> result, int limit,
            Comparator<Dict.Item> comparator) {
        ObjectArrayPriorityQueue<Dict.Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                                   comparator);
        // 前缀预测匹配
        initDictFile();
        final int maxCodeLength = prefix.length() + extendCodeLength;
        for (Map.Entry<String, Long> entry : mapTrie.predictiveSearchEntries(
                prefix)) {
            if (entry.getKey()
                     .length() > maxCodeLength) {
                continue;
            }
            loadItems(queue, entry.getValue(), prefix);
            if (queue.size() >= limit) break;
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

    @Override public int compare(Dict.Item o1, Dict.Item o2) {
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

    public void recordUserWord(Dict.Item item) {
        initH2();
        h2.updateUserItem(item);
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
                ", size=" + mapTrie.size() +
                ", sealed=" + sealed +
                '}';
    }

    private void readItems(BufferedReader reader, Converter converter) throws IOException {
        String line;
        if (itemMap == null) {
            itemMap = new TreeMap<>();
        }
        else {
            itemMap.clear();
        }
        Dict.Item item;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            String[] parts = line.split("\t");
            String text = parts[0];
            String code = converter.convert(parts[1]);
            if (parts.length > 2 && !Strings.isNullOrEmpty(parts[2])) {
                item = new Dict.Item(text, code, Integer.decode(parts[2]));
            }
            else {
                item = new Dict.Item(text, code);
            }
            if (!itemMap.containsKey(code)) {
                itemMap.put(code, new ArrayList<>());
            }
            itemMap.get(code)
                   .add(item);
        }
        reader.close();
    }

    private void build() throws IOException {
        Logs.d("build start.");
        File file = FimeContext.getInstance()
                               .fileInCacheDir(name + Dict.SUFFIX);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.writeUTF(Strings.simpleFormat("FimeDict:%s\n", name));
        final int keySize = itemMap.size();
        raf.writeShort(kVersion);  // version
        raf.writeInt(keySize);   // keySize
        final long metaOffset = raf.getFilePointer();
        for (String key : itemMap.keySet()) {
            raf.writeUTF(key);
            raf.writeLong(0L);  // placeholder to save from and to pos
        }
        StringBuilder content = new StringBuilder();
        Iterator<Map.Entry<String, List<Dict.Item>>> it = itemMap.entrySet()
                                                                 .iterator();
        long keyOffset = metaOffset;
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
            long end = raf.getFilePointer();
            raf.seek(keyOffset);
            raf.readUTF();
            raf.writeLong(start << 32 | end);
            keyOffset = raf.getFilePointer();
            raf.seek(end);
            it.remove();
        }
        sealed = true;
        itemMap = null;
        close();
        Logs.d("build end.");
    }

    private void loadItems(ObjectArrayPriorityQueue<Dict.Item> queue, long range, String code) {
        final int start = (int) (range >> 32);
        final int end = (int) range;
        try {
            raf.seek(start);
            byte[] buffer = new byte[end - start];
            raf.read(buffer);
            String content = new String(buffer);
            for (String seg : content.split("[\n]")) {
                int index = seg.indexOf('\t');
                queue.enqueue(new Dict.Item(seg.substring(0, index), code,
                                            Integer.decode(seg.substring(index + 1))));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
        if (raf == null) loadFromBuild();
    }
}
