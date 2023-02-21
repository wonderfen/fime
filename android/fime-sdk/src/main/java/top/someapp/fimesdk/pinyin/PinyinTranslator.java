package top.someapp.fimesdk.pinyin;

import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.defaults.DefaultTranslator;
import top.someapp.fimesdk.dict.Dict;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-06
 */
@Keep
public class PinyinTranslator extends DefaultTranslator {

    private static final String TAG = Fime.makeTag("PinyinTranslator");

    public PinyinTranslator() {
    }

    @Override
    public List<Candidate> translate(String selected, @NonNull List<String> code, int limit) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override public List<Candidate> translate(@NonNull List<String> code, int limit) {
        Dict dict = getDict();
        if (dict == null) {
            Log.w(TAG, "dict is invalid!");
            return Collections.EMPTY_LIST;
        }

        List<Candidate> candidates = new ArrayList<>(limit);
        StringBuilder normalized = new StringBuilder(code.size() * 6);
        for (String py : code) {
            normalized.append(py)
                      .append(" ");
        }
        normalized.deleteCharAt(normalized.length() - 1);
        if (normalized.length() > 0) {
            List<Dict.Item> items = new ArrayList<>(limit);
            boolean ok = dict.search(normalized.toString(), code.size(), items, limit);
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
            }
        }
        return candidates;
    }
}
