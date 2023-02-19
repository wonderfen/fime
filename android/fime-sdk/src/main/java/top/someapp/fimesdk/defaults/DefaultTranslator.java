package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.Translator;

import java.util.Arrays;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultTranslator implements Translator {

    private Config config;

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
    }

    @Override public void setup(@NonNull ImeEngine engine) {

    }

    @Override
    public List<Candidate> translate(String selected, @NonNull List<String> codes, int limit) {
        return innerTranslate(codes, limit);
    }

    @Override public List<Candidate> translate(@NonNull List<String> codes, int limit) {
        return innerTranslate(codes, limit);
    }

    private List<Candidate> innerTranslate(@NonNull List<String> codes, int limit) {
        StringBuilder text = new StringBuilder();
        for (String code : codes) {
            text.append(code);
        }
        return Arrays.asList(new Candidate(text.toString(), text.toString()));
    }
}
