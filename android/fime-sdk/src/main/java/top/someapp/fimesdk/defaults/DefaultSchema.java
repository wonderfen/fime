/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigValue;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.Ejector;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.api.Schema;
import top.someapp.fimesdk.api.Translator;
import top.someapp.fimesdk.utils.Classes;
import top.someapp.fimesdk.utils.Logs;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultSchema implements Schema {

    private final File appHome;
    private final File buildDir;
    private ImeEngine engine;
    private Config config;
    private String name;
    private Map<String, List<String>> options;
    private Map<String, Integer> activeOptions;
    private Map<String, Object> keyboardConfig;
    private InputEditor inputEditor;
    private Translator translator;
    private Ejector ejector;

    public DefaultSchema() {
        FimeContext fimeContext = FimeContext.getInstance();
        appHome = fimeContext.getAppHomeDir();
        buildDir = fimeContext.getCacheDir();
    }

    @Override public String getName() {
        return name;
    }

    @Override public void setName(String name) {
        this.name = name;
    }

    @Override public InputEditor getInputEditor() {
        return inputEditor;
    }

    @Override public void setInputEditor(InputEditor inputEditor) {
        this.inputEditor = inputEditor;
    }

    @Override public Translator getTranslator() {
        return translator;
    }

    @Override public void setTranslator(Translator translator) {
        this.translator = translator;
    }

    @Override public Ejector getEjector() {
        return ejector;
    }

    @Override public void setEjector(Ejector ejector) {
        this.ejector = ejector;
    }

    @Override public boolean hasOptionKey(String key) {
        return options.containsKey(key);
    }

    @Override public int getOption(String key) {
        if (hasOptionKey(key)) {
            return activeOptions.get(key);
        }
        return -1;
    }

    @Override public boolean isOptionActive(String key, int index) {
        if (hasOptionKey(key)) {
            return activeOptions.get(key) == index;
        }
        return false;
    }

    @Override public boolean activeOption(String key, int index) {
        if (hasOptionKey(key)) {
            List<String> values = options.get(key);
            if (index >= 0 && index < values.size()) {
                activeOptions.put(key, index);
                return true;
            }
        }
        return false;
    }

    @Override public void toggleOption(String key) {
        if (hasOptionKey(key)) {
            Integer i = activeOptions.get(key);
            i = (i + 1) % options.get(key)
                                 .size();
            activeOption(key, i);
        }
    }

    @Override public Map<String, Object> getKeyboardConfig() {
        return keyboardConfig;
    }

    @Override public void useKeyboardConfig(@NonNull Map<String, Object> config) {
        this.keyboardConfig = config;
    }

    @Override public void build() {
        // try {
        //     Configs.serialize(config, new FileOutputStream(new File(buildDir, "s/schema.s")));
        //     Configs.serialize(keyboards.getConfig(),
        //                       new FileOutputStream(new File(buildDir, "k/keyboards.s")));
        // }
        // catch (IOException e) {
        //     e.printStackTrace();
        // }
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
        configSelf();
        Logs.i("setupKeyboards.");
        setupKeyboards();
        Logs.i("setupInputEditor.");
        setupInputEditor();
        Logs.i("setupEjector.");
        setupEjector();
        Logs.i("setupTranslator.");
        setupTranslator();
        Logs.i("schema: " + getName() + " reconfigure OK!");
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
        inputEditor.setup(engine);
        translator.setup(engine);
        ejector.setup(engine);
    }

    private void configSelf() {
        setName(config.getString("name"));
        Config options = config.getConfig("options");
        this.options = new HashMap<>();
        this.activeOptions = new HashMap<>();
        for (Map.Entry<String, ConfigValue> entry : options.entrySet()) {
            String key = entry.getKey();
            this.options.put(key, options.getStringList(key));
            this.activeOptions.put(key, 0);
        }
    }

    private void setupKeyboards() {
        Map<String, Object> keyboards = null;
        if (config.hasPath("keyboards")) {
            try {
                keyboards = config.getConfig("keyboards")
                                  .root()
                                  .unwrapped();
            }
            catch (Exception e) {
                Logs.e(e.getMessage());
            }
        }
        if (keyboards == null) {
            keyboards = new HashMap<>();
            keyboards.put("default-layout", "qwerty");
        }
        useKeyboardConfig(keyboards);
    }

    private void setupInputEditor() {
        Config config = this.config.getConfig("inputEditor");
        try {
            inputEditor = Classes.newInstance(config.getString("type"));
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
            inputEditor = new DefaultInputEditor();
            Logs.w(e.getMessage());
        }
        inputEditor.reconfigure(config);
    }

    private void setupTranslator() {
        if (translator != null) translator.destroy();
        Config config = this.config.getConfig("translator");
        try {
            translator = Classes.newInstance(config.getString("type"));
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
            translator = new DefaultTranslator();
            Logs.w(e.getMessage());
        }
        translator.reconfigure(config);
    }

    private void setupEjector() {
        Config config = this.config.getConfig("ejector");
        try {
            ejector = Classes.newInstance(config.getString("type"));
        }
        catch (ReflectiveOperationException e) {
            e.printStackTrace();
            ejector = new DefaultEjector();
            Logs.w(e.getMessage());
        }
        ejector.reconfigure(config);
    }
}
