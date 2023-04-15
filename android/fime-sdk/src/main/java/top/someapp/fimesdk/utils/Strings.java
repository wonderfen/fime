/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * @author zwz
 * Created on 2023-02-02
 */
public class Strings {

    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";

    private Strings() {
        // no instance
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static String afterLastChar(String str, char ch) {
        if (isNullOrEmpty(str)) return str;
        int i = str.lastIndexOf(ch);
        return i + 1 < str.length() ? str.substring(i + 1) : "";
    }

    public static String simpleFormat(String format, Object... args) {
        return String.format(Locale.US, format, args);
    }

    public static String toCamelCase(String str) {
        if (isNullOrEmpty(str)) return str;
        char first = str.charAt(0);
        if (first >= 'A' && first <= 'Z') {
            return Character.toLowerCase(first) + str.substring(1);
        }
        return str;
    }

    public static String[] split(String str, String regex) {
        return str.split(regex);
    }

    public static List<String> splitByLength(String input, int codeLength) {
        assert codeLength > 0;
        final int len = input.length();
        final int count = (len + codeLength - 1) / codeLength;
        List<String> codes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            int start = i * codeLength;
            if (start < len) {
                codes.add(input.substring(start, Math.min(start + codeLength, len)));
            }
        }
        return codes;
    }

    public static String join(char delimiter, String... strings) {
        return join(delimiter, 0, strings.length, strings);
    }

    public static String join(char delimiter, int start, int end, String... strings) {
        StringBuilder rtn = new StringBuilder();
        for (int i = start; i < end; i++) {
            rtn.append(delimiter)
               .append(strings[i]);
        }
        return rtn.substring(1);
    }
}
