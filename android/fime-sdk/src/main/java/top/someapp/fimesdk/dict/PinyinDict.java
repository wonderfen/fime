/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.dict;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.databind.MappingIterator;
import org.trie4j.patricia.MapPatriciaTrie;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Serializes;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 针对拼音方案定制的词库格式
 *
 * @author zwz
 * Create on 2023-03-27
 */
class PinyinDict extends Dict {

    private static final String kIndexSuffix = ".idx";
    private MapPatriciaTrie<Long> mapTrie;        // 词条树
    private RandomAccessFile raf;

    public PinyinDict(String name) {
        super(name);
    }

    @Override
    public boolean loadFromCsv(File csvFile, Converter converter) throws IOException {
        Logs.d("normalize csv file.");
        File workDir = FimeContext.getInstance()
                                  .getWorkDir();
        CsvDict csvDict = new CsvDict(csvFile, new File(workDir, Dict.kConvertCsv));
        csvDict.normalize(FileStorage.mkdir(workDir, "temp"), converter, ' ');
        Logs.d("normalize csv file OK.");
        return build();
    }

    @Override
    public void loadFromBuild() throws IOException {
        close();

        FimeContext fimeContext = FimeContext.getInstance();
        File idxFile = fimeContext.fileInCacheDir(getName() + kIndexSuffix);
        mapTrie = Serializes.deserialize(new FileInputStream(idxFile), "FimeDict:\\S+");
        raf = new RandomAccessFile(fimeContext.fileInCacheDir(getName() + SUFFIX), "r");
    }

    @Override
    public boolean search(@NonNull String prefix, final int wordLength,
            @NonNull List<Dict.Item> result,
            int limit, Comparator<Dict.Item> comparator) {
        if (mapTrie == null || mapTrie.size() == 0) return false;

        int index = prefix.indexOf(' ');
        String first = index > 0 ? prefix.substring(0, index) : prefix;
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
                        end = mid - 1;
                    }
                }
                if (start <= end) { // hit
                    int count = 0;
                    for (int i = (start + end) / 2; i < lines.length && count < limit; i++) {
                        String line = lines[i];
                        String[] segments = line.split("\t");
                        result.add(new Item(segments[1], segments[0], Integer.decode(segments[2])));
                        count++;
                    }
                    return true;
                }
                return false;
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            for (String key : mapTrie.predictiveSearch(prefix)) {
                long range = mapTrie.get(key);
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
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        return false;
    }

    @Override
    public void close() {
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

    boolean build() throws IOException {
        FimeContext fimeContext = FimeContext.getInstance();

        File csv = new File(fimeContext.getWorkDir(), Dict.kConvertCsv);
        File idxFile = fimeContext.fileInCacheDir(getName() + kIndexSuffix);
        File file = fimeContext.fileInCacheDir(getName() + Dict.SUFFIX);

        RandomAccessFile dictRaf = new RandomAccessFile(file, "rw");
        Map<String, Integer> singleCodes = new LinkedHashMap<>(512); // 单个编码对应的索引
        MapPatriciaTrie<Long> mapTrie = new MapPatriciaTrie<>();
        MappingIterator<Item> csvIt = CsvDict.load(csv);
        int pos = 0;
        String prev = null;
        while (csvIt.hasNext()) {
            Item next = csvIt.next();
            String head = next.getFirstCode(' ');
            if (!head.equals(prev)) {
                Logs.d("found head:[%s]", head);
                singleCodes.put(head, pos);
                if (prev != null && singleCodes.containsKey(prev)) {
                    //noinspection ConstantConditions
                    long value = ((long) singleCodes.get(prev) << 32) | pos;
                    mapTrie.insert(prev, value);
                }
            }
            dictRaf.write(Strings.simpleFormat("%s\t%s\t%d\n", next.getCode(), next.getText(),
                                               next.getWeight())
                                 .getBytes(StandardCharsets.UTF_8));
            pos = (int) dictRaf.getFilePointer();
            prev = head;
        }
        dictRaf.close();
        //noinspection ConstantConditions
        long value = ((long) singleCodes.get(prev) << 32) | pos;
        mapTrie.insert(prev, value);
        mapTrie.trimToSize();
        mapTrie.freeze();
        singleCodes.clear();
        Serializes.serialize(mapTrie, new FileOutputStream(idxFile), "FimeDict:" + getName());
        //noinspection ResultOfMethodCallIgnored
        csv.delete();
        return true;
    }
}
