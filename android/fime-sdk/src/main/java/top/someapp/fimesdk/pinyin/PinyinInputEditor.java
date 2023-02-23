package top.someapp.fimesdk.pinyin;

import android.util.Log;
import androidx.annotation.Keep;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.defaults.DefaultInputEditor;
import top.someapp.fimesdk.engine.Converter;
import top.someapp.fimesdk.utils.Strings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinInputEditor extends DefaultInputEditor {

    private static final String TAG = Fime.makeTag("PinyinInputEditor");
    private Map<String, List<String>> searchCodeCache = new TreeMap<>();

    @SuppressWarnings("unchecked")
    @Override public List<String> getSearchCodes() {
        if (hasInput()) {
            List<String> codes;
            if (searchCodeCache.containsKey(getRawInput())) {
                codes = searchCodeCache.get(getRawInput());
            }
            else {
                codes = segments();
                convert(codes);
                if (codes.size() < 3) {
                    searchCodeCache.clear();
                }
                else {
                    searchCodeCache.put(getRawInput(), codes);
                }
            }
            Log.d(TAG, "searchCodes:" + codes);
            return codes;
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
                getEngine().getSchema()
                           .getEjector()
                           .eject(this);
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

    private void convert(List<String> input) {
        Converter converter = getConverter();
        if (converter == null || input.isEmpty()) return;
        for (int i = 0, len = input.size(); i < len; i++) {
            input.set(i, converter.convert(input.get(i)));
        }
    }
}
