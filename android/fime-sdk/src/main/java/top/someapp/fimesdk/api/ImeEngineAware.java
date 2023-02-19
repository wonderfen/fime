package top.someapp.fimesdk.api;

import androidx.annotation.NonNull;

/**
 * @author zwz
 * Created on 2023-02-06
 */
public interface ImeEngineAware {

    void setup(@NonNull ImeEngine engine);
}
