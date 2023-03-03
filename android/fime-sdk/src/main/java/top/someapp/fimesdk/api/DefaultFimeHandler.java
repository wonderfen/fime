package top.someapp.fimesdk.api;

import android.os.Message;
import androidx.annotation.NonNull;

/**
 * @author zwz
 * Created on 2022-12-31
 */
public class DefaultFimeHandler implements FimeHandler {

    private String name;

    public DefaultFimeHandler(@NonNull String name) {
        this.name = name;
    }

    @Override @NonNull
    public String getName() {
        return name;
    }

    @Override public void handle(@NonNull Message msg) {

    }

    @Override public boolean handleOnce(@NonNull Message msg) {
        return false;
    }

    @Override public void send(@NonNull Message msg) {

    }
}
