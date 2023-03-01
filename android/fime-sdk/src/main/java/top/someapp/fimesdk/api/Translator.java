package top.someapp.fimesdk.api;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * 翻译器
 *
 * @author zwz
 * Create on 2023-01-31
 */
public interface Translator extends ImeEngineAware, Configurable {

    int kLimit = 512;

    default List<Candidate> translate(@NonNull List<String> codes) {
        return translate(codes, getLimit());
    }

    default List<Candidate> translate(String selected, @NonNull List<String> codes) {
        return translate(selected, codes, getLimit());
    }

    default int getLimit() {
        return kLimit;
    }

    List<Candidate> translate(String selected, @NonNull List<String> codes, int limit);

    List<Candidate> translate(@NonNull List<String> codes, int limit);

    void updateDict(Candidate candidate);

    void destroy();
}
