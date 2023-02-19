package top.someapp.fimesdk.api;

/**
 * 提交器
 *
 * @author zwz
 * Create on 2023-01-31
 */
public interface Committer extends ImeEngineAware, Configurable {

    void commitText(String text);
}
