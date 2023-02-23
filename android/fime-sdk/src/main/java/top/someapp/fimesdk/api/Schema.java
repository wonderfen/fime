package top.someapp.fimesdk.api;

import top.someapp.fimesdk.view.Keyboards;

/**
 * 输入方案
 *
 * @author zwz
 * Created on 2023-02-06
 */
public interface Schema extends ImeEngineAware, Configurable {

    String getName();

    void setName(String name);

    Keyboards getKeyboards();

    void setKeyboards(Keyboards keyboards);

    boolean hasOptionKey(String key);

    int getOption(String key);

    boolean isOptionActive(String key, int index);

    boolean activeOption(String key, int index);

    void toggleOption(String key);

    InputEditor getInputEditor();

    void setInputEditor(InputEditor inputEditor);

    Translator getTranslator();

    void setTranslator(Translator translator);

    Ejector getEjector();

    void setEjector(Ejector ejector);

    void build();
}
