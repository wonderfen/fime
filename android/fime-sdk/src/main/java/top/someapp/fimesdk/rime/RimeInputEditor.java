/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.rime;

import androidx.annotation.NonNull;
import com.osfans.trime.core.Rime;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.utils.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-07-13
 */
class RimeInputEditor implements InputEditor {

    private ImeEngine engine;
    private StringBuilder rawInput = new StringBuilder();
    private List<Candidate> candidateList = new ArrayList<>();
    private int activeIndex = -1;

    @Override public Config getConfig() {
        return null;
    }

    @Override public void reconfigure(Config config) {

    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }

    @Override public boolean accept(Keycode keycode) {
        final int code = keycode.code;
        if (Keycode.isSpaceCode(code) || Keycode.isFnKeyCode(code) || Strings.isNullOrEmpty(
                keycode.label)) {
            return false;
        }

        append(keycode.label);
        return true;
    }

    @Override public String getRawInput() {
        return rawInput.toString();
    }

    @Override public List<String> getSearchCodes() {
        return Collections.singletonList(getRawInput());
    }

    @Override public void setSearchCodes(List<String> codes) {

    }

    @Override public String getPrompt() {
        Rime.RimeComposition composition = Rime.getComposition();   // “作曲”，已输入的编码组合
        return composition == null ? getRawInput() : composition.getText();
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
        return 0;
    }

    @Override public InputEditor append(String code) {
        rawInput.append(code);
        return this;
    }

    @Override public InputEditor insert(String code, int index) {
        throw new UnsupportedOperationException("Not implements!");
    }

    @Override public InputEditor backspace() {
        if (hasInput()) {
            rawInput.deleteCharAt(rawInput.length() - 1);
        }
        return this;
    }

    @Override public InputEditor delete(int index) {
        return null;
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
        if (candidate != null) {
            setActiveIndex(index);
            Rime.select_candidate_on_current_page(index);
            engine.requestSearch();
        }
    }

    @Override public Candidate getSelected() {
        return getActiveCandidate();
    }
}
