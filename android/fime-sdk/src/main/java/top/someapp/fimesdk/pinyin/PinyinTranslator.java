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
        String last = "";
        for (String py : code) {
            normalized.append(py)
                      .append(" ");
            last = py;
        }
        normalized.deleteCharAt(normalized.length() - 1);
        String remains = null;
        boolean ok = false;
        boolean again = false;
        while (normalized.length() > 0 || again) {
            List<Dict.Item> items = new ArrayList<>(limit);
            if (again) {
                ok = dict.search(normalized.toString(), code.size() - 1, items, limit);
            }
            else {
                ok = dict.search(normalized.toString(), code.size(), items, limit);
            }
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
                break;
            }
            else {  // 搜索结果为空, 回退一个编码再次查询
                if (again) break;
                normalized.delete(normalized.length() - last.length() - 1, normalized.length());
                remains = last;
                last = "";
                again = true;
            }
        }
        if (ok && remains != null) {    // 再查一下剩下的编码
            List<Dict.Item> items = new ArrayList<>(limit);
            ok = dict.search(remains, 1, items, limit);
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
            }
        }
        return candidates;
    }
}
