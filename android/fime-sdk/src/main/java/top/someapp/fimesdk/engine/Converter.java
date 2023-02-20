package top.someapp.fimesdk.engine;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 转换器：输入码 -> 查询码 的转换，如 双拼 -> 全拼
 *
 * @author zwz
 * Created on 2023-02-15
 */
public class Converter {

    private final List<String> rules;
    private int codeLength = 1;

    public Converter() {
        rules = new ArrayList<>(64);
    }

    private static String upper(String input) {
        return input.toUpperCase(Locale.US);
    }

    private static String lower(String input) {
        return input.toLowerCase(Locale.US);
    }

    private static String map(String input, String rule) {
        String[] segments = rule.split("=");
        char[] output = new char[input.length()];
        for (int i = 0, len = output.length; i < len; i++) {
            char ch = input.charAt(i);
            int index = segments[0].indexOf(ch);
            if (index >= 0) {
                output[i] = segments[1].charAt(index);
            }
            else {
                output[i] = ch;
            }
        }
        return new String(output);
    }

    public void setCodeLength(int codeLength) {
        this.codeLength = Math.min(codeLength, 1);
    }

    public void reset() {
        rules.clear();
    }

    public void addRule(@NonNull String rule) {
        rules.add(rule);
    }

    public String convert(@NonNull String input) {
        String output = input;
        for (String r : rules) {
            if (r.startsWith("U:")) {
                output = upper(output);
            }
            else if (r.startsWith("L:")) {
                output = lower(output);
            }
            else if (r.startsWith("M:")) {
                output = map(output, r.substring(2));
            }
            else if (r.startsWith("R:")) {
                output = replace(output, r.substring(2));
            }
        }
        return output;
    }

    private String replace(String input, String rule) {
        String[] segments = rule.split("=");
        return input.replaceAll(segments[0], segments[1]);
    }
}
