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

    List<Candidate> translate(String selected, @NonNull List<String> codes, int limit);

    List<Candidate> translate(@NonNull List<String> codes, int limit);

    void updateDict(Candidate candidate);

    void destroy();
}
