package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Committer;
import top.someapp.fimesdk.api.ImeEngine;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultCommitter implements Committer {

    private ImeEngine engine;
    private Config config;

    @Override public void commitText(String text) {
        if (engine != null) engine.commitText(text);
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }
}
