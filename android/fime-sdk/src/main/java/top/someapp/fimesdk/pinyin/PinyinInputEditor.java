/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.pinyin;

import androidx.annotation.Keep;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.defaults.DefaultInputEditor;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinInputEditor extends DefaultInputEditor {

    private final Stack<String> searchCodes = new Stack<>();
    private String lastSegment = "";

    @Override public String getLastSegment() {
        return lastSegment;
    }

    @SuppressWarnings("unchecked")
    @Override public List<String> getSearchCodes() {
        lastSegment = "";
        if (hasInput()) {
            List<String> codes = segments();    // 分段
            while (!searchCodes.isEmpty() && searchCodes.size() >= codes.size()) {
                searchCodes.pop();
            }
            if (codes.isEmpty()) {
                searchCodes.push("");
            }
            else {
                lastSegment = codes.get(codes.size() - 1);
                searchCodes.push(getConverter().convert(lastSegment));
            }
            Logs.d("searchCodes:", searchCodes);
            return searchCodes;
        }
        return Collections.EMPTY_LIST;
    }

    @Override public void select(int index) {
        Candidate candidate = getCandidateAt(index);
        if (candidate == null) return;

        boolean accept;
        List<String> searchCodes = getSearchCodes();
        Candidate selected = getSelected();
        if (selected == null) {
            accept = candidate.code.startsWith(searchCodes.get(0));
        }
        else {
            accept = candidate.code.startsWith(searchCodes.get(selected.text.length()));
        }
        if (accept) {
            addSelected(candidate);
            if (getSelected().text.length() >= searchCodes.size()) {
                getEngine().manualEject();
            }
        }
    }

    private List<String> segments() {
        final Syncopate syncopate = getSyncopate();
        List<String> groups = new ArrayList<>(64);
        char delimiter = getDelimiter();
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
        return groups;
    }
}
