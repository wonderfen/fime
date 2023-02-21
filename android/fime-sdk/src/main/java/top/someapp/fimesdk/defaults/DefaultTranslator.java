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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultTranslator implements Translator {

    private ImeEngine engine;
    private Config config;
    private Dict dict;
    private transient File target;

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
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

    protected ImeEngine getEngine() {
        return engine;
    }

    protected Dict getDict() {
        return dict;
    }

    private List<Candidate> innerTranslate(@NonNull List<String> codes, int limit) {
        StringBuilder text = new StringBuilder();
        for (String code : codes) {
            text.append(code);
        }
        return Arrays.asList(new Candidate(text.toString(), text.toString()));
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
            if (FileStorage.hasFile(fimeContext.fileInCacheDir(name + ".s"))) {
                dict = Dict.loadFromCompiled(
                        new FileInputStream(fimeContext.fileInCacheDir(name + ".s")));
            }
            else {
                dict = new Dict(name);
                if (c.hasPath("converter") && c.hasPath("converter.rules")) {
                    Converter converter = new Converter();
                    for (String rule : c.getStringList("converter.rules")) {
                        converter.addRule(rule);
                    }
                    dict.loadFromCsv(fimeContext.fileInAppHome(c.getString("file")), converter);
                }
                else {
                    dict.loadFromCsv(fimeContext.fileInAppHome(c.getString("file")));
                }
                target = fimeContext.fileInCacheDir(name + ".s");
                compileDictIf();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
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
