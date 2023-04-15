/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.Translator;
import top.someapp.fimesdk.dict.Dict;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultTranslator implements Translator {

    private static final int kMaxUserWordLength = 7;
    private ImeEngine engine;
    private Config config;
    private Dict dict;
    private transient File dictSource;
    private transient Converter converter;
    private int limit = kLimit;

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
        if (config.hasPath("limit")) {
            limit = Math.max(1, config.getInt("limit"));
        }
        initDict();
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
        compileDictIf();
    }

    @Override
    public List<Candidate> translate(String selected, @NonNull List<String> codes, int limit) {
        return innerTranslate(codes, limit);
    }

    @Override public List<Candidate> translate(@NonNull List<String> codes, int limit) {
        return innerTranslate(codes, limit);
    }

    @Override public void updateDict(Candidate candidate) {
        if (dict != null && candidate.text.length() <= kMaxUserWordLength) { // 记录到用户词
            dict.recordUserWord(new Dict.Item(candidate.text, candidate.code));
        }
    }

    @Override public void destroy() {
        if (dict != null) {
            dict.close();
            dict = null;
        }
    }

    @Override public int getLimit() {
        return limit;
    }

    protected ImeEngine getEngine() {
        return engine;
    }

    protected Dict getDict() {
        return dict;
    }

    @SuppressWarnings("unused")
    private List<Candidate> innerTranslate(@NonNull List<String> codes, int limit) {
        StringBuilder text = new StringBuilder();
        for (String code : codes) {
            text.append(code);
        }
        return Collections.singletonList(new Candidate(text.toString(), text.toString()));
    }

    private void initDict() {
        Config c = config.getConfig("dict");
        String name = c.getString("name");
        if (dict != null && dict.getName()
                                .equals(name)) {
            return;
        }

        FimeContext fimeContext = FimeContext.getInstance();
        try {
            char delimiter = c.hasPath("delimiter") ? c.getString("delimiter")
                                                       .charAt(0) : '\0';
            dict = new Dict(name, delimiter);
            if (FileStorage.hasFile(fimeContext.fileInCacheDir(name + Dict.SUFFIX))) {
                dict.loadFromBuild();
            }
            else {
                dictSource = fimeContext.fileInAppHome(c.getString("file"));
                if (c.hasPath("converter") && c.hasPath("converter.rules")) {
                    converter = new Converter();
                    for (String rule : c.getStringList("converter.rules")) {
                        converter.addRule(rule);
                    }
                }
                compileDictIf();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
    }

    private void compileDictIf() {
        if (dict == null || engine == null || dictSource == null) return;
        engine.post(() -> {
            try {
                if (converter == null) {
                    dict.loadFromCsv(dictSource);
                }
                else {
                    dict.loadFromCsv(dictSource, converter);
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                Logs.w(e.getMessage());
            }
            dictSource = null;
            converter = null;
        });
    }
}
