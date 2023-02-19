package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import top.someapp.fimesdk.api.Syncopate;

import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultSyncopate implements Syncopate {

    @Override public boolean isValidCode(@NonNull String code) {
        return true;
    }

    @Override public String segments(@NonNull String input, @NonNull List<String> result) {
        return innerSegments(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter) {
        return innerSegments(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter,
            int from) {
        return innerSegments(input, result);
    }

    private String innerSegments(@NonNull String input, @NonNull List<String> result) {
        result.add(input);
        return "";
    }
}
