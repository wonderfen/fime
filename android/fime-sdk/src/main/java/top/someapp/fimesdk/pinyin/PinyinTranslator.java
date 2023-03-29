package top.someapp.fimesdk.pinyin;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.defaults.DefaultTranslator;
import top.someapp.fimesdk.dict.Dict;
import top.someapp.fimesdk.utils.Logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinTranslator extends DefaultTranslator {

    public PinyinTranslator() {
    }

    @Override
    public List<Candidate> translate(String selected, @NonNull List<String> code, int limit) {
        return translate(code.subList(selected.length(), code.size()), limit);
    }

    @SuppressWarnings("unchecked")
    @Override public List<Candidate> translate(@NonNull List<String> code, int limit) {
        Dict dict = getDict();
        if (dict == null) {
            Logs.w("dict is invalid!");
            return Collections.EMPTY_LIST;
        }

        Logs.d("translate: " + code);
        List<Candidate> candidates = new ArrayList<>(limit);
        StringBuilder normalized = new StringBuilder(code.size() * 6);
        boolean hasDelimiter = dict.getDelimiter() > 0;
        if (hasDelimiter) {
            for (String py : code) {
                normalized.append(py)
                          .append(dict.getDelimiter());
            }
        }
        else {
            for (String py : code) {
                normalized.append(py);
            }
        }
        if (hasDelimiter && normalized.length() > 0) {
            normalized.deleteCharAt(normalized.length() - 1);
            List<Dict.Item> items = new ArrayList<>(limit);
            boolean ok = dict.search(normalized.toString(), code.size(), items, limit);
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
            }
        }
        if (code.size() > 1 && candidates.size() < 9) {
            List<Dict.Item> items = new ArrayList<>(limit);
            boolean ok;
            if (hasDelimiter) {
                ok = dict.search(normalized.substring(0, normalized.lastIndexOf(" ")),
                                 code.size() - 1,
                                 items, limit);
            }
            else {
                ok = dict.search(normalized.toString(), items, limit);
            }
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
                int size = items.size();
                dict.search(code.get(code.size() - 1), 1, items, limit);
                for (int i = size; i < items.size(); i++) {
                    Dict.Item item = items.get(i);
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
            }
        }
        return candidates;
    }
}
