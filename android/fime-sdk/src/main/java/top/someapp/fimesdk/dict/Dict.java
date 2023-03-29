package top.someapp.fimesdk.dict;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.MappingIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import org.trie4j.patricia.MapPatriciaTrie;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Serializes;
import top.someapp.fimesdk.utils.Strings;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    static final String kConvertCsv = "convert.csv";
    static final short kVersion = 2;       // 版本号
    private static final int kMaxTableHeadLength = 2;   // 码表词库的最长索引长度
    private static H2 h2;   // 用户词词典
    private final String name;  // 词典名
    private final char delimiter;
    private transient long tireOffset;
    private MapPatriciaTrie<Long> mapTrie;        // 词条树
    private int size;           // 包含的词条数
    private boolean sealed;     // 词典是否构建完成
    private RandomAccessFile raf;

    public Dict(@NonNull String name) {
        this(name, '\0');
    }

    public Dict(@NonNull String name, char delimiter) {
        this.name = name;
        this.delimiter = delimiter;
        Logs.d("create dict: %s.", name);
    }

    @VisibleForTesting
    static Dict createPinyinDict(@NonNull String name) {
        return new PinyinDict(name);
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

    public char getDelimiter() {
        return delimiter;
    }

    public boolean loadFromCsv(File csvFile) throws IOException {
        return loadFromCsv(csvFile, null);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean loadFromCsv(File csvFile, Converter converter) throws IOException {
        Logs.d("normalize csv file.");
        File workDir = FimeContext.getInstance()
                                  .getWorkDir();
        CsvDict csvDict = new CsvDict(csvFile, new File(workDir, kConvertCsv));
        csvDict.normalize(FileStorage.mkdir(workDir, "temp"), converter, delimiter);
        Logs.d("normalize csv file OK.");
        return build();
    }

    public void loadFromBuild() throws IOException {
        File build = FimeContext.getInstance()
                                .fileInCacheDir(name + SUFFIX);
        raf = new RandomAccessFile(build, "r");
        String head = raf.readUTF();
        if (!head.startsWith("FimeDict:")) throw new IOException("Unknown file format!");
        short version = raf.readShort();
        if (version < 1 || version > kVersion) {
            throw new UnsupportedEncodingException("Not supported version: " + version + "!");
        }
        long trieOffset = raf.readLong();
        @SuppressWarnings("unused")
        int keySize = raf.readInt();
        raf.seek(trieOffset);
        byte[] buffer = new byte[(int) (raf.length() - trieOffset)];
        int len = raf.read(buffer);
        mapTrie = Serializes.deserialize(new ByteArrayInputStream(buffer, 0, len));
        sealed = true;
        size = mapTrie.size();
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
        Logs.d("search: %s start.", prefix);
        // 优先查询用户词
        initH2();   // 耗时操作
        initDictFile(); // 耗时操作
        List<Item> userItems = h2.queryUserItems(prefix, limit);
        if (userItems.isEmpty()) searchWithTrie(prefix, wordLength, result, limit);
        mergeResult(userItems, result, limit, comparator);
        Logs.d("search: %s end.", prefix);
        return !result.isEmpty();
    }

    public boolean searchPrefix(@NonNull String prefix, final int extendCodeLength,
            @NonNull List<Item> result, int limit,
            Comparator<Item> comparator) {
        Logs.d("searchPrefix: %s start.", prefix);
        ObjectArrayPriorityQueue<Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                              comparator);
        // 优先查询用户词
        initH2();   // 耗时操作
        // 前缀预测匹配
        initDictFile(); // 耗时操作
        final int maxCodeLength = prefix.length() + extendCodeLength;
        List<Item> userItems = h2.queryUserItems(prefix, limit);
        if (userItems.isEmpty()) searchWithTrie(prefix, maxCodeLength, result, limit);
        mergeResult(userItems, result, limit, comparator);
        Logs.d("searchPrefix: %s end.", prefix);
        return !result.isEmpty();
    }

    public void recordUserWord(Item item) {
        initH2();
        h2.updateUserItem(item);
    }

    public void close() {
        Logs.d("close dict: %s.", name);
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

    @SuppressWarnings("unused") @VisibleForTesting
    MapPatriciaTrie<Long> getMapTrie() {
        return mapTrie;
    }

    private void searchWithTrie(String prefix, final int wordLength,
            @NonNull List<Item> result, int limit) {
        if (mapTrie == null || mapTrie.size() == 0) return;

        String first;
        if (delimiter > 0) {
            int index = prefix.indexOf(delimiter);
            first = index > 0 ? prefix.substring(0, index) : prefix;
        }
        else {
            first = prefix.length() > kMaxTableHeadLength ? prefix.substring(0,
                                                                             kMaxTableHeadLength) : prefix;
        }
        Logs.d("search: [%s] start.", first);
        if (mapTrie.contains(first)) {
            long range = mapTrie.get(first);
            int start = (int) (range >>> 32);
            int end = (int) range;
            int len = first.length();
            try {
                raf.seek(start);
                byte[] bytes = new byte[end - start];
                raf.read(bytes);
                String content = new String(bytes, StandardCharsets.UTF_8);
                String[] lines = content.split("[\n]");
                start = 0;
                end = lines.length - 1;
                while (len <= prefix.length() && start < end) {
                    Logs.d("start: %d, end: %d", start, end);
                    int mid = (start + end) / 2;
                    String line = lines[mid];
                    String[] segments = line.split("\t");
                    String code = segments[0];
                    int diff = code.compareTo(prefix);
                    if (diff < 0) { // code before prefix, -->
                        start = mid + 1;
                    }
                    else if (diff == 0) {   // code near prefix, <--
                        end = mid;
                    }
                    else {  // code after prefix, <--
                        end = mid /*- 1*/;
                    }
                }
                if (start <= end) { // hit
                    int count = 0;
                    for (int i = (start + end) / 2; i < lines.length && count < limit; i++) {
                        String line = lines[i];
                        String[] segments = line.split("\t");
                        if (!segments[0].startsWith(prefix)) break;
                        result.add(new Item(segments[1], segments[0], Integer.decode(segments[2])));
                        count++;
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            for (String key : mapTrie.predictiveSearch(prefix)) {
                loadItems(mapTrie.get(key), result, limit);
                break;
            }
        }
    }

    private void loadItems(long range, List<Item> result, int limit) {
        int start = (int) (range >>> 32);
        int end = (int) range;
        byte[] bytes = new byte[end - start];
        try {
            raf.seek(start);
            raf.read(bytes);
            String content = new String(bytes, StandardCharsets.UTF_8);
            String[] lines = content.split("[\n]");
            for (int i = 0, min = Math.min(limit, lines.length); i < min; i++) {
                String[] segments = lines[i].split("[\t]", 3);
                result.add(new Item(segments[1], segments[0], Integer.decode(segments[2])));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeResult(List<Item> userItems, List<Item> result,
            int limit, Comparator<Item> comparator) {
        ObjectArrayPriorityQueue<Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                              comparator);
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
    }

    private void initH2() {
        if (h2 == null) {
            h2 = new H2(name);
            h2.start();
        }
        else {
            if (!name.equals(h2.getId())) {
                h2.stop();
                h2 = new H2(name);
            }
            h2.start();
        }
    }

    private void initDictFile() {
        if (raf == null) {
            try {
                loadFromBuild();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void writeHead(RandomAccessFile raf) throws IOException {
        raf.writeUTF(Strings.simpleFormat("FimeDict:%s", name));
        raf.writeShort(kVersion);  // version
        tireOffset = raf.getFilePointer();
        raf.writeLong(0L);  // placeholder to save tire offset
    }

    private void writeItems(RandomAccessFile raf) throws IOException {
        FimeContext fimeContext = FimeContext.getInstance();
        mapTrie = new MapPatriciaTrie<>();
        File csv = new File(fimeContext.getWorkDir(), kConvertCsv);
        String prev = null;
        String head;
        int pos = 0;
        Map<String, Integer> singleCodes = new LinkedHashMap<>(512); // 单个编码对应的索引
        MappingIterator<Item> it = CsvDict.load(csv);
        Logs.d("write all dict items.");
        while (it.hasNext()) {
            Item next = it.next();
            if (delimiter > 0) {
                head = next.getFirstCode(delimiter);
            }
            else {
                if (next.getCode()
                        .length() <= kMaxTableHeadLength) {
                    head = next.getCode();
                }
                else {
                    head = next.getCode()
                               .substring(0, kMaxTableHeadLength);
                }
            }
            if (!head.equals(prev)) {
                Logs.d("found head:[%s]", head);
                singleCodes.put(head, pos);
                if (prev != null && singleCodes.containsKey(prev)) {
                    //noinspection ConstantConditions
                    long value = ((long) singleCodes.get(prev) << 32) | pos;
                    mapTrie.insert(prev, value);
                }
            }
            raf.write(Strings.simpleFormat("%s\t%s\t%d\n", next.getCode(), next.getText(),
                                           next.getWeight())
                             .getBytes(StandardCharsets.UTF_8));
            pos = (int) raf.getFilePointer();
            prev = head;
        }
        //noinspection ConstantConditions
        long value = ((long) singleCodes.get(prev) << 32) | pos;
        mapTrie.insert(prev, value);
        singleCodes.clear();
        //noinspection ResultOfMethodCallIgnored
        csv.delete();
        Logs.d("write all dict items OK.");
    }

    private void writeTrie(RandomAccessFile raf) throws IOException {
        Logs.d("write dict trie.");
        mapTrie.trimToSize();
        mapTrie.freeze();
        long pos = raf.getFilePointer();
        raf.seek(tireOffset);
        raf.writeLong(pos);
        raf.seek(pos);
        ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
        OutputStream out = new OutputStream() {
            @Override public void write(int b) throws IOException {
                if (buffer.remaining() == 0) {
                    raf.write(buffer.array());
                    buffer.clear();
                }
                buffer.put((byte) b);
            }
        };
        Serializes.serialize(mapTrie, out);
        Logs.d("write dict trie OK.");
        if (buffer.position() > 0) {
            byte[] bytes = buffer.array();
            raf.write(bytes, 0, buffer.position());
        }
    }

    private boolean build() throws IOException {
        if (sealed) return false;

        Logs.d("build dict start.");
        FimeContext fimeContext = FimeContext.getInstance();
        File file = fimeContext.fileInCacheDir(name + SUFFIX);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");

        writeHead(raf);
        writeItems(raf);
        writeTrie(raf);

        raf.close();
        Logs.d("build dict end.");
        return true;
    }

    @Keep
    public static class Item implements Serializable, Comparable<Item> {

        private String text;
        private String code;
        private int weight;

        @Keep @SuppressWarnings("unused")
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

        void setCode(String code) {
            this.code = code;
        }

        public int getWeight() {
            return weight;
        }

        public void setWeight(int weight) {
            this.weight = weight;
        }

        @JsonIgnore
        public int getLength() {
            return getText().length();
        }

        @JsonIgnore
        public String getFirstCode(char delimiter) {
            return code.indexOf(delimiter) > 0 ? code.substring(0, code.indexOf(delimiter)) : code;
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
