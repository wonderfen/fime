/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.table;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.defaults.DefaultTranslator;
import top.someapp.fimesdk.dict.Dict;
import top.someapp.fimesdk.utils.Logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-16
 */
@Keep
public class TableTranslator extends DefaultTranslator implements Comparator<Dict.Item> {

    private int searchCodeExtend = 0;

    public TableTranslator() {
    }

    @Override public void reconfigure(Config config) {
        super.reconfigure(config);
        if (config.hasPath("search-code-extend")) {
            searchCodeExtend = Math.max(config.getInt("search-code-extend"), 0);
        }
    }

    @Override
    public List<Candidate> translate(String selected, @NonNull List<String> codes, int limit) {
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override public List<Candidate> translate(@NonNull List<String> codes, int limit) {
        Dict dict = getDict();
        if (dict == null) {
            Logs.w("dict is invalid!");
            return Collections.EMPTY_LIST;
        }

        List<Candidate> candidates = new ArrayList<>(limit);
        for (String code : codes) {
            List<Dict.Item> items = new ArrayList<>(limit);
            boolean ok = dict.searchPrefix(code, searchCodeExtend, items, limit, this);
            if (ok) {
                for (Dict.Item item : items) {
                    candidates.add(new Candidate(item.getCode(), item.getText()));
                }
            }
            else {
                break;
            }
        }
        return candidates;
    }

    @SuppressWarnings("all")
    @Override public int compare(Dict.Item o1, Dict.Item o2) {
        String code1 = o1.getCode();
        String code2 = o2.getCode();
        if (code1.equals(code2)) { // 编码相同时，短词优先
            return o1.getLength() <= o2.getLength() ? -1 : o1.getLength() - o2.getLength();
        }
        if (code1.length() == code2.length()) {
            return o2.getWeight() >= o1.getWeight() ? -1 : o2.getWeight() - o1.getWeight();
        }
        if (o1.getWeight() >= o2.getWeight()) return -1;
        if (code1.length() <= code2.length()) return -1;
        return o2.getWeight() - o1.getWeight();
    }
}
