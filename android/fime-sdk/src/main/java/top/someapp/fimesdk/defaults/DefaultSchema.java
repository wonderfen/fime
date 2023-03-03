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
import top.someapp.fimesdk.config.Configs;
import top.someapp.fimesdk.utils.Classes;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.view.Keyboards;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultSchema implements Schema {

    private ImeEngine engine;
    private Config config;
    private String name;
    private Map<String, List<String>> options;
    private Map<String, Integer> activeOptions;
    private Keyboards keyboards;
    private InputEditor inputEditor;
    private Translator translator;
    private Ejector ejector;
    private File appHome;
    private File buildDir;

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

    @Override public Keyboards getKeyboards() {
        return keyboards;
    }

    @Override public void setKeyboards(Keyboards keyboards) {
        this.keyboards = keyboards;
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
        if (keyboards != null) keyboards.setup(engine);
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
        String file = config.getString("keyboards");
        try {
            Config config;
            if (FileStorage.hasFile(buildDir, file + ".s")) {
                config = Configs.deserialize(new File(buildDir, file + ".s"));
            }
            else {
                config = Configs.load(new File(appHome, file), true);
            }
            keyboards = new Keyboards(config);
        }
        catch (IOException e) {
            e.printStackTrace();
            Logs.e("setupKeyboards error:%s", e.getMessage());
        }
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
