/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.rime;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Ejector;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.api.Schema;
import top.someapp.fimesdk.api.Translator;
import top.someapp.fimesdk.defaults.DefaultEjector;

import java.util.Map;

/**
 * @author zwz
 * Created on 2023-07-13
 */
public class RimeSchema implements Schema {

    private String name;
    private ImeEngine engine;
    private InputEditor inputEditor;
    private Ejector ejector;

    public RimeSchema() {
        inputEditor = new RimeInputEditor();
    }

    @Override public Config getConfig() {
        return null;
    }

    @Override public void reconfigure(Config config) {

    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
        this.ejector = new DefaultEjector();
        inputEditor.setup(engine);
        ejector.setup(engine);
    }

    @Override public String getName() {
        return name;
    }

    @Override public void setName(String name) {
        this.name = name;
    }

    @Override public boolean hasOptionKey(String key) {
        return false;
    }

    @Override public int getOption(String key) {
        return 0;
    }

    @Override public boolean isOptionActive(String key, int index) {
        return false;
    }

    @Override public boolean activeOption(String key, int index) {
        return false;
    }

    @Override public void toggleOption(String key) {

    }

    @Override public Map<String, Object> getKeyboardConfig() {
        return null;
    }

    @Override public void useKeyboardConfig(@NonNull Map<String, Object> config) {

    }

    @Override public InputEditor getInputEditor() {
        return inputEditor;
    }

    @Override public void setInputEditor(InputEditor inputEditor) {
        this.inputEditor = inputEditor;
    }

    @Override public Translator getTranslator() {
        return null;
    }

    @Override public void setTranslator(Translator translator) {
        throw new UnsupportedOperationException("Rime 方案不支持翻译器！");
    }

    @Override public Ejector getEjector() {
        return ejector;
    }

    @Override public void setEjector(Ejector ejector) {
        throw new UnsupportedOperationException("Not implements!");
    }

    @Override public void build() {

    }
}
