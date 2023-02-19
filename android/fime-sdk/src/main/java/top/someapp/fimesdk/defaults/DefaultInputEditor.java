package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.config.Keycode;

import java.util.ArrayList;
import java.util.Arrays;
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
    private List<Candidate> selected;       // 已选择的候选

    public DefaultInputEditor() {
        rawInput = new StringBuilder();
        candidateList = new ArrayList<>();
        selected = new ArrayList<>();
    }

    @Override public void setup(@NonNull ImeEngine engine) {

    }

    @Override public boolean accept(Keycode keycode) {
        if (Keycode.isLetterLowerCode(keycode.code)) {
            append(keycode.label);
            return true;
        }
        return false;
    }

    @Override public String getRawInput() {
        return rawInput.toString();
    }

    @Override public List<String> getSearchCodes() {
        return Arrays.asList(getRawInput());
    }

    @Override public void setSearchCodes(List<String> codes) {

    }

    @Override public String getFormattedInput() {
        return getRawInput();
    }

    @Override public InputEditor clearInput() {
        rawInput.setLength(0);
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
        if (candidate != null) selected.add(candidate);
    }

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
    }
}
