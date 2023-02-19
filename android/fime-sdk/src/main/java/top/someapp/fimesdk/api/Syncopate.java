package top.someapp.fimesdk.api;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * 输入码切分器，如 xian => xi'an
 *
 * @author zwz
 * Created on 2023-02-06
 */
public interface Syncopate {

    boolean isValidCode(@NonNull String code);

    String segments(@NonNull String input, @NonNull List<String> result);

    String segments(@NonNull String input, @NonNull List<String> result, char delimiter);

    String segments(@NonNull String input, @NonNull List<String> result, char delimiter, int from);
}
