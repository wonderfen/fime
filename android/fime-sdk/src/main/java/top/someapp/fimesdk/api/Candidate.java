package top.someapp.fimesdk.api;

import androidx.annotation.NonNull;

/**
 * @author zwz
 * Created on 2022-12-26
 */
public class Candidate {

    public final String code;
    public final String text;

    public Candidate(String code, String text) {
        this.code = code;
        this.text = text;
    }

    public Candidate append(@NonNull Candidate other) {
        return new Candidate(code + " " + other.code, text + other.text);
    }

    @NonNull @Override public String toString() {
        return code + '\t' + text;
    }
}
