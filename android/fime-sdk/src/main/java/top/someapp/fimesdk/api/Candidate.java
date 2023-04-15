/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import androidx.annotation.NonNull;

import java.util.Objects;

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

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Candidate candidate = (Candidate) o;
        if (!Objects.equals(code, candidate.code)) return false;

        return Objects.equals(text, candidate.text);
    }

    @Override public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (text != null ? text.hashCode() : 0);
        return result;
    }

    public Candidate append(@NonNull Candidate other) {
        if (code == null || code.isEmpty()) {
            return new Candidate(other.code, text + other.text);
        }
        return new Candidate(code + " " + other.code, text + other.text);
    }

    @NonNull @Override public String toString() {
        return code + '\t' + text;
    }
}
