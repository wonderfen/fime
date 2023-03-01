package top.someapp.fimesdk.syncopate;

import androidx.annotation.NonNull;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.utils.Strings;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zwz
 * Created on 2023-03-01
 */
class RegexSyncopate implements Syncopate {

    private final Pattern pattern;

    RegexSyncopate(String regex) {
        pattern = Pattern.compile(regex);
    }

    @Override public String segments(@NonNull String input, @NonNull List<String> result) {
        return groups(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter) {
        return groups(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter,
            int from) {
        return groups(input.substring(from), result);
    }

    private String groups(String input, List<String> result) {
        Matcher matcher = pattern.matcher(input);
        boolean found = false;
        while (matcher.find()) {
            if (!found) found = true;
            result.add(matcher.group());
        }
        return found ? Strings.EMPTY_STRING : input;
    }
}
