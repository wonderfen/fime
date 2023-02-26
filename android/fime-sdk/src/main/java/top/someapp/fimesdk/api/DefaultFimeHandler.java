package top.someapp.fimesdk.api;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author zwz
 * Created on 2022-12-31
 */
public class DefaultFimeHandler extends Handler implements FimeHandler {

    private final Callback callback;
    private String name;

    public DefaultFimeHandler(@NonNull Looper looper, String name) {
        super(looper);
        this.name = name;
        this.callback = null;
    }

    public DefaultFimeHandler(@NonNull Looper looper, String name, @Nullable Callback callback) {
        super(looper, callback);
        this.name = name;
        this.callback = callback;
    }

    @Override public void handleMessage(@NonNull Message msg) {
        if (callback == null) {
            super.handleMessage(msg);
        }
        else {
            callback.handleMessage(msg);
        }
    }

    @Override @NonNull
    public String getName() {
        return name;
    }
}
