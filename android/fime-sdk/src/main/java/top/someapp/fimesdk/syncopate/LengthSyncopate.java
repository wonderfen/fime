/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.syncopate;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.utils.Strings;

import java.util.List;

/**
 * @author zwz
 * Created on 2023-03-01
 */
@Keep
class LengthSyncopate implements Syncopate {

    private final int length;

    LengthSyncopate(int length) {
        this.length = Math.max(1, length);
    }

    @Override public boolean isValidCode(@NonNull String code) {
        return !Strings.isNullOrEmpty(code) && code.length() <= length;
    }

    @Override public String segments(@NonNull String input, @NonNull List<String> result) {
        return segmentsByLength(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter) {
        return segmentsByLength(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter,
            int from) {
        return segmentsByLength(input.substring(from), result);
    }

    private String segmentsByLength(@NonNull String input, @NonNull List<String> result) {
        result.addAll(Strings.splitByLength(input, length));
        return Strings.EMPTY_STRING;
    }
}
