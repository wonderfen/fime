package top.someapp.fimesdk.pinyin;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Committer;
import top.someapp.fimesdk.api.ImeEngine;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinCommitter implements Committer {

    private ImeEngine engine;
    private Config config;

    @Override public void commitText(String text) {
        if (engine != null) engine.commitText(text);
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
    }
}
