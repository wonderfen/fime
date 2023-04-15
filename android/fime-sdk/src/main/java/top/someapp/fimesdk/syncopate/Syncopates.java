/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.syncopate;

import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.utils.Strings;

/**
 * @author zwz
 * Created on 2023-03-01
 */
public class Syncopates {

    private Syncopates() {
        // no instance.
    }

    public static Syncopate create(String expression) {
        if (Strings.isNullOrEmpty(expression) || expression.equals("whole")) {
            return new WholeSyncopate();
        }
        if (expression.equals("pinyin")) {
            return new PinyinSyncopate();
        }
        if (expression.startsWith("length:")) {
            return new LengthSyncopate(Integer.decode(expression.substring(7)));
        }
        if (expression.startsWith("regex:")) {
            return new RegexSyncopate(expression.substring(6));
        }
        // fallback to whole.
        return new WholeSyncopate();
    }
}
