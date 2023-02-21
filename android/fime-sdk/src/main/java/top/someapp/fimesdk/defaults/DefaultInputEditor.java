package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
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
    private StringBuilder selected;         // 已选择的候选
    private String alphabet = "qwertyuiopasdfghjklzxcvbnm";
    private String initials = "qwertyuiopasdfghjklzxcvbnm";
    private char delimiter = '\0';
    private Integer codeLength;
    private Syncopate syncopate;
    private Converter converter;

    public DefaultInputEditor() {
        rawInput = new StringBuilder();
        candidateList = new ArrayList<>();
        selected = new StringBuilder();
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

    @Override public List<String> getSearchCodes() {
        return Collections.singletonList(getRawInput());
    }

    @Override public void setSearchCodes(List<String> codes) {

    }

    @Override public String getFormattedInput() {
        StringBuilder prompt = new StringBuilder();
        if (getCursor() <= 0) {
            for (String code : getSearchCodes()) {
                prompt.append("'")
                      .append(code);
            }
            if (prompt.length() > 0) {
                prompt.deleteCharAt(0);
                if (rawInput.charAt(rawInput.length() - 1) == delimiter) {
                    prompt.append(delimiter);
                }
            }
        }
        else {
            prompt.append(selected);
            if (getCursor() < rawInput.length()) prompt.append(rawInput.substring(getCursor()));
        }
        return prompt.toString();
    }

    @Override public InputEditor clearInput() {
        rawInput.setLength(0);
        selected.setLength(0);
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
        if (hasInput()) rawInput.deleteCharAt(rawInput.length() - 1);
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
        Candidate candidate = getCandidateAt(index);
        if (candidate == null) return;
        selected.append(candidate.text);
        // FIXME: 2023/1/29 candidate.code.length() 始终等于 rawInput.length()!!
        cursor += candidate.code.length();
        if (cursor >= rawInput.length()) {
            engine.commitText(getSelected());
        }
        else {
            engine.commitText(getFormattedInput());
        }
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

    protected Syncopate createSyncopate() {
        return null;
    }

    protected ImeEngine getEngine() {
        return engine;
    }

    protected String getSelected() {
        return selected.toString();
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
