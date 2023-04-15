/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.utils;

/**
 * @author zwz
 * Create on 2023-01-09
 */
public class Nulls {

    private Nulls() {
        // no instance.
    }

    public static <T> T firstNonNull(T... ts) {
        for (T t : ts) {
            if (t != null) return t;
        }
        return null;
    }
}
