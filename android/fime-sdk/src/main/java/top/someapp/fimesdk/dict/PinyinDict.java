package top.someapp.fimesdk.dict;

import androidx.annotation.NonNull;
import com.fasterxml.jackson.databind.MappingIterator;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
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
class PinyinDict {

    private final String name;
    private final Map<String, Integer> singleCodes; // 单个编码对应的索引
    private RandomAccessFile raf;   // 搜索时使用的词库文件

    PinyinDict(String name) {
        this.name = name;
        singleCodes = new LinkedHashMap<>(512);
    }

    public String getName() {
        return name;
    }

    boolean loadFromCsv(File csvFile, Converter converter) throws IOException {
        Logs.d("normalize csv file.");
        File workDir = FimeContext.getInstance()
                                  .getWorkDir();
        CsvDict csvDict = new CsvDict(csvFile, new File(workDir, Dict.kConvertCsv));
        csvDict.normalize(FileStorage.mkdir(workDir, "temp"), converter);
        Logs.d("normalize csv file OK.");
        return build();
    }

    void loadFromBuild() throws IOException {
    }

    boolean search(@NonNull String prefix, final int wordLength, @NonNull List<Dict.Item> result,
            int limit, Comparator<Dict.Item> comparator) {
        return false;
    }

    boolean build() throws IOException {
        FimeContext fimeContext = FimeContext.getInstance();
        File file = fimeContext.fileInCacheDir(name + Dict.SUFFIX);
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.writeUTF(Strings.simpleFormat("FimeDict:%s", name));
        raf.writeShort(Dict.kVersion);
        final long indexOffset = raf.getFilePointer();
        raf.writeLong(0L);  // index start pos
        raf.writeLong(0L);  // index end pos
        raf.write(new byte[512 * 14]);  // 预留的索引信息的存储空间
        File csv = new File(fimeContext.getWorkDir(), Dict.kConvertCsv);

        String prev;
        List<Dict.Item> items = new ArrayList<>();
        MappingIterator<Dict.Item> it = CsvDict.load(csv);
        while (it.hasNext()) {
            Dict.Item next = it.next();
            if (next.getLength() > 10) continue;    // 跳过词长超过 10 的词条

            if (singleCodes.containsKey(next.getFirstCode())) {
                // do nothing.
            }
            else {
                singleCodes.put(next.getFirstCode(), (int) raf.getFilePointer());
            }
            byte[] itemBytes = new byte[128];
            // 这里需要定长！！
            raf.writeUTF(next.getCode());
            raf.writeUTF(next.getText());
            raf.writeInt(next.getWeight());
            prev = next.getCode();
        }
        //noinspection ResultOfMethodCallIgnored
        csv.delete();
        return false;
    }

    void close() {
    }
}
