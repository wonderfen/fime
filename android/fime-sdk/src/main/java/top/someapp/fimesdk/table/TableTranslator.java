package top.someapp.fimesdk.table;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.Translator;
import top.someapp.fimesdk.dict.Dict;
import top.someapp.fimesdk.pinyin.PinyinTranslator;
import top.someapp.fimesdk.utils.FileStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-16
 */
@Keep
public class TableTranslator implements Translator, Comparator<Dict.Item> {

    private ImeEngine engine;
    private Config config;
    private String searchMethod = "search";
    private Dict dict;
    private transient File target;

    public TableTranslator() {
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
        if (config.hasPath("search-method")) {
            searchMethod = config.getString("search-method");
        }
        Config c = config.getConfig("dict");
        String name = c.getString("name");
        String file = null;
        if (c.hasPath("file")) {
            file = c.getString("file");
        }
        initDict(name, file);
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
        compileDictIf();
    }

    @Override
    public List<Candidate> translate(String selected, @NonNull List<String> codes, int limit) {
        return null;
    }

    @Override public List<Candidate> translate(@NonNull List<String> codes, int limit) {
        List<Candidate> candidates = new ArrayList<>(limit);
        StringBuilder normalized = new StringBuilder(codes.size() * 6);
        for (String py : codes) {
            normalized.append(py)
                      .append(" ");
        }
        normalized.deleteCharAt(normalized.length() - 1);
        if (normalized.length() > 0) {
            List<Dict.Item> items = new ArrayList<>(limit);
            boolean ok;
            if ("search".equals(searchMethod)) {
                ok = dict.search(normalized.toString(), codes.size() + 1, items, limit, this);
            }
            else {
                ok = dict.searchPrefix(normalized.toString(), codes.size() + 1, items, limit, this);
            }
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
            }
        }
        return candidates;
    }

    @Override public int compare(Dict.Item o1, Dict.Item o2) {
        String code1 = o1.getCode();
        String code2 = o2.getCode();
        if (code1.equals(code2)) { // 编码相同时，短词优先
            return o1.getLength() - o2.getLength();
        }
        if (code1.length() == code2.length()) {
            return o2.getWeight() - o1.getWeight();
        }
        if (o1.getWeight() > 0 && o2.getWeight() > 0) {
            return code1.length() - code2.length();
        }
        else {
            return o2.getWeight() - o1.getWeight();
        }
    }

    private void initDict(String name, String file) {
        if (dict == null) {
            FimeContext fimeContext = FimeContext.getInstance();
            try {
                if (file.endsWith(".csv")) {
                    if (FileStorage.hasFile(fimeContext.fileInCacheDir(file + ".s"))) {
                        dict = Dict.loadFromCompiled(
                                new FileInputStream(fimeContext.fileInCacheDir(file + ".s")));
                    }
                    else {
                        dict = new Dict(name);
                        dict.loadFromCsv(fimeContext.fileInAppHome(file));
                        target = fimeContext.fileInCacheDir(file + ".s");
                        compileDictIf();
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (dict == null || dict.getSize() == 0) {
            try {
                dict = Dict.loadFromCompiled(
                        PinyinTranslator.class.getResourceAsStream("/pinyin.dict"));
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void compileDictIf() {
        if (dict == null || engine == null || target == null) return;
        if (!target.exists()) {
            engine.post(() -> {
                try {
                    dict.compileTo(new FileOutputStream(target));
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                target = null;
            });
        }
    }
}
