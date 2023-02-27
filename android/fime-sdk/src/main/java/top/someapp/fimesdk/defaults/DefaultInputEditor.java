package top.someapp.fimesdk.defaults;

import android.util.Log;
import android.util.Pair;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValueType;
import top.someapp.fimesdk.Fime;
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
import java.util.Stack;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultInputEditor implements InputEditor {

    private static final String TAG = Fime.makeTag("DefaultInputEditor");
    private static final int kMaxInputCodeLength = 64;
    private ImeEngine engine;
    private Config config;
    private StringBuilder rawInput;         // 已输入的原始编码
    private List<Candidate> candidateList;  // 候选列表
    // 记录选字/词时光标的位置, first: rawInput.length, second: text.length
    private Stack<Pair<Integer, Integer>> selectedCursor;
    private int activeIndex;                // 选中候选的索引
    private Candidate selected;             // 已选择的候选
    private String alphabet = "qwertyuiopasdfghjklzxcvbnm";
    private String initials = "qwertyuiopasdfghjklzxcvbnm";
    private char delimiter = '\0';
    private Integer codeLength;
    private Syncopate syncopate;
    private Converter converter;

    public DefaultInputEditor() {
        rawInput = new StringBuilder();
        candidateList = new ArrayList<>();
        selectedCursor = new Stack<>();
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }

    @Override public boolean accept(Keycode keycode) {
        final int code = keycode.code;
        if (Keycode.isFnKeyCode(code) || Strings.isNullOrEmpty(keycode.label)) return false;

        if (hasInput()) {
            if (rawInput.length() >= kMaxInputCodeLength) {
                Log.w(TAG, "input too long!");
                return false;
            }
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
        if (selectedCursor.isEmpty()) return Collections.singletonList(getRawInput());
        if (getCursor() >= rawInput.length()) return Collections.EMPTY_LIST;
        return Collections.singletonList(rawInput.substring(getCursor()));
    }

    @Override public void setSearchCodes(List<String> codes) {

    }

    @Override public String getPrompt() {
        String prompt = rawInputAsPrompt();
        if (config.hasPath("prompt")) {
            final ConfigValueType type = config.getValue("prompt")
                                               .valueType();
            switch (type) {
                case STRING:
                    if ("searchCode".equals(config.getString("prompt"))) {
                        prompt = searchCodeAsPrompt();
                    }
                    break;
                case OBJECT:
                    return remapKeyAsPrompt();
                default:
                    break;
            }
        }
        Log.d(TAG, "prompt: [" + prompt + "]");
        return prompt;
    }

    @Override public InputEditor clearInput() {
        rawInput.setLength(0);
        selected = null;
        selectedCursor.clear();
        getSearchCodes();
        return this;
    }

    @Override public InputEditor clearCandidates() {
        candidateList.clear();
        return this;
    }

    @Override public int getCursor() {
        return rawInput.length();
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
            if (selectedCursor.isEmpty()) {
                Log.d(TAG, "backspace input code.");
                rawInput.deleteCharAt(rawInput.length() - 1);
            }
            else {
                Pair<Integer, Integer> pair = selectedCursor.peek();
                if (pair.first >= rawInput.length()) {
                    removeLastSelected();
                }
                else {
                    rawInput.deleteCharAt(rawInput.length() - 1);
                }
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
        setActiveIndex(index);
        getEngine().eject();
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
        converter = new Converter();
        if (config.hasPath("converter")) {
            Config c = config.getConfig("converter");
            if (c.hasPath("rules")) {
                for (String rule : c.getStringList("rules")) {
                    converter.addRule(rule);
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
        if (selected != null) prompt.append(selected.text);
        for (int i = prompt.length(); i < searchCodes.size(); i++) {
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
        selectedCursor.push(new Pair<>(rawInput.length(), candidate.text.length()));
        clearCandidates();
        getEngine().requestSearch();
    }

    protected void removeLastSelected() {
        if (selectedCursor.isEmpty()) return;

        Log.d(TAG, "removeLastSelected.");
        selectedCursor.pop();
        if (selectedCursor.isEmpty()) {
            selected = null;
        }
        else {
            Pair<Integer, Integer> last = selectedCursor.peek();
            String[] codes = selected.code.split("[ ]");
            String code = Strings.join(' ', 0, last.second, codes);
            String text = selected.text.substring(0, last.second);
            selected = new Candidate(code, text);
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
