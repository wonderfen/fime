/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import androidx.annotation.NonNull;

import java.util.Map;

/**
 * 输入方案
 *
 * @author zwz
 * Created on 2023-02-06
 */
public interface Schema extends ImeEngineAware, Configurable {

    String getName();

    void setName(String name);

    boolean hasOptionKey(String key);

    int getOption(String key);

    boolean isOptionActive(String key, int index);

    boolean activeOption(String key, int index);

    void toggleOption(String key);

    Map<String, Object> getKeyboardConfig();

    void useKeyboardConfig(@NonNull Map<String, Object> config);

    InputEditor getInputEditor();

    void setInputEditor(InputEditor inputEditor);

    Translator getTranslator();

    void setTranslator(Translator translator);

    Ejector getEjector();

    void setEjector(Ejector ejector);

    void build();
}
