package top.someapp.fimesdk.pinyin;

import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinInputEditor implements InputEditor {

    private static final String TAG = Fime.makeTag("PinyinInputEditor");
    private ImeEngine engine;
    private Config config;
    private StringBuilder rawInput;         // 已输入的原始编码
    private int cursor;                     // 已输入编码的光标位置
    private List<Candidate> candidateList;  // 候选列表
    private int activeIndex;                // 选中候选的索引
    private List<Candidate> selected;       // 已选择的候选
    private String alphabet = "qwertyuiopasdfghjklzxcvbnm";
    private String initials = "qwertyuiopasdfghjklzxcvbnm";
    private char delimiter = '\0';
    private Integer codeLength;
    private Syncopate syncopate;
    private Converter converter;

    public PinyinInputEditor() {
        Log.d(TAG, Strings.simpleFormat("create InputEditor: 0x%x.", hashCode()));
        rawInput = new StringBuilder();
        cursor = 0;
        candidateList = new ArrayList<>();
        selected = new ArrayList<>();
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
        if (hasInput()) {
            List<String> codes = segments();
            convert(codes);
            return codes;
        }
        return Collections.EMPTY_LIST;
    }

    @Override public void setSearchCodes(List<String> codes) {

    }

    @Override public String getFormattedInput() {
        StringBuilder formatted = new StringBuilder();
        int i = 0;
        for (Candidate s : selected) {
            formatted.append(s.text);
            i += s.code.length();
        }
        if (i < rawInput.length()) {
            // List<String> codes = new ArrayList<>(64);
            // String remains = syncopate.segments(rawInput.substring(i), codes);
            // for (String code : codes) {
            //     formatted.append(" ")
            //              .append(code);
            // }
            // if (!Strings.isNullOrEmpty(remains)) formatted.append(remains);
            formatted.append(rawInput.substring(i));
        }
        return formatted.toString();
    }

    @Override public InputEditor clearInput() {
        Log.d(TAG, "clearInput");
        // rawInput.setLength(0);
        if (rawInput.length() > 0) rawInput.delete(0, rawInput.length());
        selected.clear();
        cursor = 0;
        return this;
    }

    @Override public InputEditor clearCandidates() {
        Log.d(TAG, "clearCandidates");
        candidateList.clear();
        return this;
    }

    @Override public int getCursor() {
        return cursor;
    }

    @Override public InputEditor append(String code) {
        Log.d(TAG, "append: " + code);
        rawInput.append(code);
        return this;
    }

    @Override public InputEditor insert(String code, int index) {
        rawInput.insert(index, code);
        return this;
    }

    @Override public InputEditor backspace() {
        delete(rawInput.length() - 1);
        return this;
    }

    @Override public InputEditor delete(int index) {
        rawInput.deleteCharAt(index);
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
        selected.add(candidate);
        // FIXME: 2023/1/29 candidate.code.length() 始终等于 rawInput.length()!!
        cursor += candidate.code.length();
        if (cursor >= rawInput.length()) {
            engine.commitText(getFormattedInput());
            clearInput();
            clearCandidates();
            setActiveIndex(0);
        }
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
        if (config == null || config.isEmpty()) return;
        if (config.hasPath("alphabet")) alphabet = config.getString("alphabet");
        if (config.hasPath("initials")) initials = config.getString("initials");
        if (config.hasPath("delimiter")) {
            delimiter = config.getString("delimiter")
                              .charAt(0);
        }
        codeLength = null;
        if (config.hasPath("code-length")) codeLength = config.getInt("code-length");
        if (config.hasPath("syncopate") && config.getBoolean("syncopate")) {
            syncopate = new PinyinSyncopate();
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

    private List<String> segments() {
        List<String> groups = new ArrayList<>(64);
        if (syncopate == null) {
            if (delimiter > '\0') {
                groups.addAll(
                        Arrays.asList(getRawInput().split(
                                Strings.simpleFormat("[\\u%04x]", (int) delimiter))));
            }
            else if (codeLength != null && codeLength > 0) {
                String rawInput = getRawInput();
                StringBuilder temp = new StringBuilder(codeLength);
                for (char ch : rawInput.toCharArray()) {
                    if (temp.length() < codeLength) {
                        temp.append(ch);
                        continue;
                    }
                    groups.add(temp.toString());
                    temp.setLength(0);
                    temp.append(ch);
                }
                if (temp.length() > 0) groups.add(temp.toString());
            }
            else {
                groups.add(getRawInput());
            }
        }
        else {
            if (syncopate.isValidCode(getRawInput())) {
                groups.add(getRawInput());
            }
            else {
                String remains;
                if (delimiter > '\0') {
                    remains = syncopate.segments(getRawInput(), groups, delimiter);
                }
                else {
                    remains = syncopate.segments(getRawInput(), groups);
                }
                if (!Strings.isNullOrEmpty(remains)) groups.add(remains);
            }
        }
        return groups;
    }

    private void convert(List<String> input) {
        if (converter == null || input.isEmpty()) return;
        for (int i = 0, len = input.size(); i < len; i++) {
            input.set(i, converter.convert(input.get(i)));
        }
    }
}
