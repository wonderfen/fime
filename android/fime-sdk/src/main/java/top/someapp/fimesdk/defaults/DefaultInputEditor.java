package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultInputEditor implements InputEditor {

    private ImeEngine engine;
    private Config config;
    private StringBuilder rawInput;         // 已输入的原始编码
    private int cursor;                     // 已输入编码的光标位置
    private List<Candidate> candidateList;  // 候选列表
    private int activeIndex;                // 选中候选的索引
    private Candidate selected;         // 已选择的候选
    private String alphabet = "qwertyuiopasdfghjklzxcvbnm";
    private String initials = "qwertyuiopasdfghjklzxcvbnm";
    private char delimiter = '\0';
    private Integer codeLength;
    private Syncopate syncopate;
    private Converter converter;

    public DefaultInputEditor() {
        rawInput = new StringBuilder();
        candidateList = new ArrayList<>();
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }

    @Override public boolean accept(Keycode keycode) {
        final int code = keycode.code;
        if (Keycode.isFnKeyCode(code) || Strings.isNullOrEmpty(keycode.label)) return false;

        if (hasInput()) {
            if (alphabet.contains(keycode.label)) {
                append(keycode.label);
                return true;
            }
        }
        else {
            if (initials.contains(keycode.label)) {
                append(keycode.label);
                return true;
            }
        }
        return false;
    }

    @Override public String getRawInput() {
        return rawInput.toString();
    }

    @SuppressWarnings("unchecked")
    @Override public List<String> getSearchCodes() {
        if (getCursor() >= rawInput.length()) return Collections.EMPTY_LIST;
        return Collections.singletonList(rawInput.substring(getCursor()));
    }

    @Override public void setSearchCodes(List<String> codes) {

    }

    @Override public String getPrompt() {
        if (!config.hasPath("prompt")) return rawInputAsPrompt();

        final ConfigValueType type = config.getValue("prompt")
                                           .valueType();
        switch (type) {
            case STRING:
                if ("rawInput".equals(config.getString("prompt"))) {
                    return rawInputAsPrompt();
                }
                if ("searchCode".equals(config.getString("prompt"))) {
                    return searchCodeAsPrompt();
                }
                else {
                    return rawInputAsPrompt();
                }
            case OBJECT:
                return remapKeyAsPrompt();
            default:
                return rawInputAsPrompt();
        }
    }

    @Override public InputEditor clearInput() {
        rawInput.setLength(0);
        selected = null;
        cursor = 0;
        return this;
    }

    @Override public InputEditor clearCandidates() {
        candidateList.clear();
        return this;
    }

    @Override public int getCursor() {
        return cursor;
    }

    @Override public InputEditor append(String code) {
        rawInput.append(code);
        return this;
    }

    @Override public InputEditor insert(String code, int index) {
        rawInput.insert(index, code);
        return this;
    }

    @Override public InputEditor backspace() {
        if (hasInput()) {
            rawInput.deleteCharAt(rawInput.length() - 1);
            if (cursor >= rawInput.length()) {
                removeLastSelected();
            }
        }
        return this;
    }

    @Override public InputEditor delete(int index) {
        if (hasInput()) {
            if (index >= 0 && index < rawInput.length()) {
                rawInput.deleteCharAt(index);
            }
        }
        return this;
    }

    @Override public List<Candidate> getCandidateList() {
        return candidateList;
    }

    @Override public void appendCandidate(Candidate candidate) {
        candidateList.add(candidate);
    }

    @Override public Candidate getCandidateAt(int index) {
        if (hasCandidate() && index >= 0 && index < candidateList.size()) {
            return candidateList.get(index);
        }
        return null;
    }

    @Override public Candidate getActiveCandidate() {
        return getCandidateAt(getActiveIndex());
    }

    @Override public boolean hasInput() {
        return rawInput.length() > 0;
    }

    @Override public boolean hasCandidate() {
        return !candidateList.isEmpty();
    }

    @Override public int getActiveIndex() {
        return activeIndex;
    }

    @Override public InputEditor setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
        return this;
    }

    @Override public void select(int index) {
        // 具体子类实现!
    }

    @Override public Candidate getSelected() {
        return selected;
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
        if (config.hasPath("alphabet")) alphabet = config.getString("alphabet");
        if (config.hasPath("initials")) initials = config.getString("initials");
        if (config.hasPath("delimiter")) {
            delimiter = config.getString("delimiter")
                              .charAt(0);
        }
        codeLength = null;
        if (config.hasPath("code-length")) codeLength = config.getInt("code-length");
        if (config.hasPath("syncopate") && config.getBoolean("syncopate")) {
            syncopate = createSyncopate();
        }
        converter = null;
        if (config.hasPath("converter")) {
            Config converter = config.getConfig("converter");
            this.converter = new Converter();
            if (converter.hasPath("rules")) {
                for (String rule : converter.getStringList("rules")) {
                    this.converter.addRule(rule);
                }
            }
        }
    }

    protected String rawInputAsPrompt() {
        return getRawInput();
    }

    protected String searchCodeAsPrompt() {
        StringBuilder prompt = new StringBuilder();
        List<String> searchCodes = getSearchCodes();
        if (selected != null) {
            prompt.append(selected.text);
        }
        for (int i = getCursor(); i < searchCodes.size(); i++) {
            prompt.append("'")
                  .append(searchCodes.get(i));
        }
        if (prompt.length() > 0) {
            if (prompt.charAt(0) == '\'') prompt.deleteCharAt(0);
            if (rawInput.charAt(rawInput.length() - 1) == delimiter) prompt.append(delimiter);
        }
        return prompt.toString();
    }

    protected String remapKeyAsPrompt() {
        StringBuilder prompt = new StringBuilder();
        if (selected != null) prompt.append(selected.text);
        ConfigObject map = config.getObject("prompt");
        for (int i = getCursor(), len = rawInput.length(); i < len; i++) {
            String key = rawInput.substring(i, i + 1);
            if (map.containsKey(key)) {
                prompt.append(map.toConfig()
                                 .getString(key));
            }
            else {
                prompt.append(key);
            }
        }
        return prompt.toString();
    }

    protected Syncopate createSyncopate() {
        return null;
    }

    protected ImeEngine getEngine() {
        return engine;
    }

    protected void addSelected(Candidate candidate) {
        if (selected == null) {
            selected = candidate;
        }
        else {
            selected = selected.append(candidate);
        }
        cursor = selected.text.length();
    }

    protected void removeLastSelected() {
        if (selected == null) return;
        if (selected.code.contains(" ")) {
            String code = selected.code.replaceFirst("[ ].*$", "");
            String text = selected.text.substring(0, selected.text.length() - 1);
            selected = new Candidate(code, text);
            cursor = selected.text.length();
        }
        else {
            selected = null;
            cursor = 0;
        }
    }

    protected String getAlphabet() {
        return alphabet;
    }

    protected String getInitials() {
        return initials;
    }

    protected char getDelimiter() {
        return delimiter;
    }

    protected Integer getCodeLength() {
        return codeLength;
    }

    protected Syncopate getSyncopate() {
        return syncopate;
    }

    protected Converter getConverter() {
        return converter;
    }
}
