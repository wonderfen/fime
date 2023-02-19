package top.someapp.fimesdk.pinyin;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.Translator;
import top.someapp.fimesdk.dict.Dict;
import top.someapp.fimesdk.utils.FileStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinTranslator implements Translator {

    private ImeEngine engine;
    private Config config;
    private Dict dict;
    private transient File target;

    public PinyinTranslator() {
        // try {
        //     dict = Dict.loadFromCompiled(
        //             PinyinTranslator.class.getResourceAsStream("/pinyin.dict"));
        // }
        // catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    @Override
    public List<Candidate> translate(String selected, @NonNull List<String> code, int limit) {
        return null;
    }

    @Override public List<Candidate> translate(@NonNull List<String> code, int limit) {
        List<Candidate> candidates = new ArrayList<>(limit);
        StringBuilder normalized = new StringBuilder(code.size() * 6);
        for (String py : code) {
            normalized.append(py)
                      .append(" ");
        }
        normalized.deleteCharAt(normalized.length() - 1);
        if (normalized.length() > 0) {
            List<Dict.Item> items = new ArrayList<>(limit);
            boolean ok = dict.search(normalized.toString(), code.size(), items, limit);
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
            }
        }
        return candidates;
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
        compileDictIf();
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
        Config c = config.getConfig("dict");
        String name = c.getString("name");
        String file = null;
        if (c.hasPath("file")) {
            file = c.getString("file");
        }
        initDict(name, file);
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
