/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.dict;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import org.trie4j.patricia.MapPatriciaTrie;
import org.trie4j.patricia.MapPatriciaTrieNode;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Serializes;
import top.someapp.fimesdk.utils.Strings;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 使用 H2Database 存取的词典
 *
 * @author zwz
 * Created on 2023-03-24
 */
class DictH2 implements Comparator<Dict.Item> {

    public static final String SUFFIX = ".dic"; // 生成词典的后缀名
    private static final short kVersion = 3;       // 版本号
    private static final String kConvertCsv = "convert.csv";
    private static H2 h2;   // 用户词词典
    private final String name;  // 词典名
    private MapPatriciaTrie<Long> mapTrie;        // 词条树
    private int size;           // 包含的词条数
    private boolean sealed;     // 词典是否构建完成
    private RandomAccessFile raf;

    DictH2(String name) {
        this.name = name;
        Logs.d("create dict: %s.", name);
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

    public int getSize() {
        return size;
    }

    public boolean loadFromCsv(File csvFile) throws IOException {
        return loadFromCsv(csvFile, new Converter());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean loadFromCsv(File csvFile, @NonNull Converter converter) throws IOException {
        Logs.d("loadFromCsv.");
        loadSortConvert(csvFile, converter);
        Logs.d("build.");
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
        @SuppressWarnings("unused")
        int keySize = raf.readInt();
        long trieOffset = raf.readLong();
        raf.seek(trieOffset);
        byte[] buffer = new byte[(int) (raf.length() - trieOffset)];
        int len = raf.read(buffer);
        mapTrie = Serializes.deserialize(new ByteArrayInputStream(buffer, 0, len));
        sealed = true;
        size = mapTrie.size();
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
        Logs.d("search: %s start.", prefix);
        ObjectArrayPriorityQueue<Dict.Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                                   comparator);
        // 优先查询用户词
        initH2();   // 耗时操作
        initDictFile();
        List<Dict.Item> userItems = h2.queryUserItems(prefix, limit);
        if (mapTrie.contains(prefix)) { // 全部匹配
            MapPatriciaTrieNode<Long> node = mapTrie.getNode(prefix);
            final long range = node.getValue(); // [start, end)
            final int start = (int) (range >> 32);
            final int end = (int) range;
            try {
                raf.seek(start);
                byte[] buffer = new byte[end - start];
                raf.read(buffer);
                String content = new String(buffer, StandardCharsets.UTF_8);
                for (String record : content.split("[\n]")) {
                    int index = record.indexOf('\t');
                    String text = record.substring(0, index);
                    queue.enqueue(
                            new Dict.Item(text, prefix,
                                          Integer.decode(record.substring(index + 1))));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {    // 前缀预测匹配
            int length = 0;
            List<String> codes = new ArrayList<>();
            List<Long> ranges = new ArrayList<>();   // 先分段
            for (Map.Entry<String, Long> entry : mapTrie.predictiveSearchEntries(prefix)) {
                final String code = entry.getKey();
                if (length == 0) length = code.length();
                if (code.length() > length) continue;
                codes.add(entry.getKey());
                ranges.add(entry.getValue());
            }
            // 统一一次 io
            if (!ranges.isEmpty()) {
                final int start = (int) (ranges.get(0) >> 32);
                final int end = ranges.get(ranges.size() - 1)
                                      .intValue();
                try {
                    raf.seek(start);
                    byte[] buffer = new byte[end - start];
                    raf.read(buffer);
                    for (int i = 0, len = codes.size(); queue.size() < limit && i < len; i++) {
                        String code = codes.get(i);
                        long range = ranges.get(i);
                        int offset = (int) (range >> 32) - start;
                        int size = (int) (range - (range >> 32));
                        String content = new String(buffer, offset, size, StandardCharsets.UTF_8);
                        for (String record : content.split("[\n]")) {
                            int index = record.indexOf('\t');
                            String text = record.substring(0, index);
                            // if (wordLength < 0 || text.length() == wordLength) {
                            //     queue.enqueue(new Item(text, code,
                            //                            Integer.decode(
                            //                                    record.substring(index + 1))));
                            // }
                            queue.enqueue(new Dict.Item(text, code,
                                                        Integer.decode(
                                                                record.substring(index + 1))));
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                // if (queue.size() >= limit) break;
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
        Logs.d("search: %s end.", prefix);
        return hit;
    }

    public boolean searchPrefix(@NonNull String prefix, final int extendCodeLength,
            @NonNull List<Dict.Item> result, int limit,
            Comparator<Dict.Item> comparator) {
        Logs.d("searchPrefix: %s start.", prefix);
        ObjectArrayPriorityQueue<Dict.Item> queue = new ObjectArrayPriorityQueue<>(limit * 2,
                                                                                   comparator);
        // 优先查询用户词
        initH2();   // 耗时操作
        // 前缀预测匹配
        initDictFile();
        final int maxCodeLength = prefix.length() + extendCodeLength;
        for (Map.Entry<String, Long> entry : mapTrie.predictiveSearchEntries(
                prefix)) {
            final long range = entry.getValue(); // [start, end)
            final int start = (int) (range >> 32);
            final int end = (int) range;
            final String code = entry.getKey();
            if (code.length() > maxCodeLength) continue;
            try {
                raf.seek(start);
                byte[] buffer = new byte[end - start];
                raf.read(buffer);
                String content = new String(buffer, StandardCharsets.UTF_8);
                for (String record : content.split("[\n]")) {
                    int index = record.indexOf('\t');
                    String text = record.substring(0, index);
                    queue.enqueue(
                            new Dict.Item(text, code, Integer.decode(record.substring(index + 1))));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                break;
            }
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
        Logs.d("searchPrefix: %s end.", prefix);
        return hit;
    }

    public void recordUserWord(Dict.Item item) {
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

    @Override public int compare(Dict.Item o1, Dict.Item o2) {
        return Dict.compareItems(o1, o2);
    }

    @SuppressWarnings("unused") @VisibleForTesting
    MapPatriciaTrie<Long> getMapTrie() {
        return mapTrie;
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

    @SuppressWarnings("ResultOfMethodCallIgnored") private boolean build() throws IOException {
        if (sealed) return false;

        FimeContext fimeContext = FimeContext.getInstance();
        File file = fimeContext.fileInCacheDir(name + SUFFIX);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.writeUTF(Strings.simpleFormat("FimeDict:%s", name));
        // final int keySize = itemMap.size();
        raf.writeShort(kVersion);  // version
        raf.writeInt(0);   // keySize
        final long metaOffset = raf.getFilePointer();
        raf.writeLong(0L);  // placeholder to save tire offset
        StringBuilder content = new StringBuilder();
        MapPatriciaTrie<Long> mapTrie = new MapPatriciaTrie<>();

        File csv = new File(fimeContext.getWorkDir(), kConvertCsv);
        BufferedReader reader = new BufferedReader(new FileReader(csv));
        String prev = null;
        String line;
        while ((line = reader.readLine()) != null) {
            String[] segments = line.split("\t");
            String text = segments[0];
            String code = segments[1];
            String weight = segments[2];
            if (!code.equals(prev)) {
                if (content.length() > 0) {
                    long start = raf.getFilePointer();
                    raf.write(content.toString()
                                     .getBytes(StandardCharsets.UTF_8));
                    content.setLength(0);
                    mapTrie.insert(prev, start << 32 | raf.getFilePointer());
                }
            }
            content.append(text)
                   .append("\t")
                   .append(weight)
                   .append("\n");
            prev = code;
        }
        if (content.length() > 0) {
            long start = raf.getFilePointer();
            raf.write(content.toString()
                             .getBytes(StandardCharsets.UTF_8));
            content.setLength(0);
            mapTrie.insert(prev, start << 32 | raf.getFilePointer());
        }
        reader.close();
        csv.delete();

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
        return true;
    }

    @SuppressWarnings("all")
    private void loadSortConvert(File csv, @NonNull Converter converter) throws IOException {
        Logs.d("clearItems.");
        initH2();
        h2.clearItems();
        BufferedReader reader = new BufferedReader(new FileReader(csv));
        String line;
        int count = 0;
        Logs.d("load from csv");
        final int batchSize = 1024;
        List<Dict.Item> items = new ArrayList<>(batchSize);
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#")) continue;
            String[] segments = line.split("\t");
            if (segments.length < 2) continue;

            String text = segments[0];
            StringBuilder code = new StringBuilder();
            for (String each : segments[1].split("[ ]")) {
                code.append(" ")
                    .append(converter.convert(each));
            }
            Dict.Item item;
            if (segments.length == 3) {
                item = new Dict.Item(text, code.substring(1), Integer.decode(segments[2]));
            }
            else {
                item = new Dict.Item(text, code.substring(1));
            }
            count++;
            items.add(item);
            if (count % batchSize == 0) {
                // Logs.d("insertItems");
                h2.insertItems(items);
                items.clear();
            }
        }
        reader.close();
        if (!items.isEmpty()) h2.insertItems(items);

        File workDir = FimeContext.getInstance()
                                  .getWorkDir();
        Writer writer = new FileWriter(new File(workDir, kConvertCsv));
        int start = 0;
        while (start < count) {
            List<Dict.Item> sort = h2.queryItems(" code asc ", start, batchSize);
            if (sort.isEmpty()) break;
            for (Dict.Item item : sort) {
                writer.write(
                        Strings.simpleFormat("%s\t%s\t%d\n", item.getText(), item.getCode(),
                                             item.getWeight()));
            }
            writer.flush();
            if (sort.size() < batchSize) break;
            start += batchSize;
        }
        Logs.d("dropDict.");
        h2.dropDict();
        writer.close();
    }
}
