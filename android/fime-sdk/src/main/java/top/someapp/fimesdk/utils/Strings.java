package top.someapp.fimesdk.utils;

import java.util.Locale;

/**
 * @author zwz
 * Created on 2023-02-02
 */
public class Strings {

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

    public static String join(char delimiter, String... strings) {
        StringBuilder rtn = new StringBuilder();
        for (String s : strings) {
            rtn.append(delimiter)
               .append(s);
        }
        return rtn.substring(1);
    }
}
