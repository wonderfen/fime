package top.someapp.fimesdk.api;

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

    @Override public String toString() {
        return code + '\t' + text;
    }
}
