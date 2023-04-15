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
class WholeSyncopate implements Syncopate {

    @Override public String segments(@NonNull String input, @NonNull List<String> result) {
        return whole(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter) {
        return whole(input, result);
    }

    @Override
    public String segments(@NonNull String input, @NonNull List<String> result, char delimiter,
            int from) {
        return whole(input, result);
    }

    private String whole(@NonNull String input, @NonNull List<String> result) {
        result.add(input);
        return Strings.EMPTY_STRING;
    }
}
