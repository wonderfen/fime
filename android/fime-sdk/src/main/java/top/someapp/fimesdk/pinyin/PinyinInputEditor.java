package top.someapp.fimesdk.pinyin;

import android.util.Log;
import androidx.annotation.Keep;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.defaults.DefaultInputEditor;
import top.someapp.fimesdk.utils.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinInputEditor extends DefaultInputEditor {

    private static final String TAG = Fime.makeTag("PinyinInputEditor");
    private Stack<String> searchCodes = new Stack<>();
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
            Log.d(TAG, "searchCodes:" + searchCodes);
            return searchCodes;
        }
        return Collections.EMPTY_LIST;
    }

    @Override public boolean accept(Keycode keycode) {
        if (super.accept(keycode)) {
            return true;
        }
        return false;
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
                getEngine().eject();
            }
        }
    }

    @Override protected Syncopate createSyncopate() {
        return new PinyinSyncopate();
    }

    private List<String> segments() {
        List<String> groups = new ArrayList<>(64);
        Syncopate syncopate = getSyncopate();
        char delimiter = getDelimiter();
        Integer codeLength = getCodeLength();
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
}
