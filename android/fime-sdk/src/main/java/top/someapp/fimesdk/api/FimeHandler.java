package top.someapp.fimesdk.api;

import android.os.Message;
import androidx.annotation.NonNull;

/**
 * @author zwz
 * Created on 2022-12-31
 */
public interface FimeHandler {

    @NonNull String getName();

    void handleMessage(@NonNull Message msg);

    boolean sendEmptyMessage(int what);

    boolean sendEmptyMessageDelayed(int what, long delayMillis);

    boolean post(@NonNull Runnable work);
}
