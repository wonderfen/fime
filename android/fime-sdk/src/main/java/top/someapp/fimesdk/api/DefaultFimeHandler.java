package top.someapp.fimesdk.api;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author zwz
 * Created on 2022-12-31
 */
public class DefaultFimeHandler extends Handler implements FimeHandler {

    private String name;

    public DefaultFimeHandler(@NonNull Looper looper, String name) {
        super(looper);
        this.name = name;
    }

    public DefaultFimeHandler(@NonNull Looper looper, String name, @Nullable Callback callback) {
        super(looper, callback);
        this.name = name;
    }

    @Override @NonNull
    public String getName() {
        return name;
    }
}
