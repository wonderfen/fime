/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk.api;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import androidx.annotation.NonNull;
import top.someapp.fimesdk.view.VirtualKey;

/**
 * @author zwz
 * Created on 2023-02-06
 */
public interface ImeEngine extends KeyEvent.Callback {

    int CN_MODE = 1;
    int ASCII_MODE = 2;

    ImeState getState();

    void enterState(ImeState newState);

    void setMode(int mode);

    ImeEngine useInputMethodService(InputMethodService ims);

    Context getContext();

    Looper getWorkLopper();

    Schema getSchema();

    ImeEngine useSchema(String conf);

    void onStartInput(EditorInfo attribute, boolean restarting);

    void onTap(@NonNull VirtualKey virtualKey);

    void requestSearch();

    void manualEject();

    void commitText(String text);

    void registerHandler(@NonNull FimeHandler handler);

    void unregisterHandler(@NonNull String name);

    void notifyHandlers(@NonNull Message message);

    void notifyHandlersDelay(@NonNull Message message, int delayMills);

    void post(Runnable work);

    enum ImeState {
        QUIT,       // 退出
        FREEZE,     // 启动后 InputView 还未显示或被隐藏
        READY,      // 已启动 InputView 已显示，等待输入编码
        INPUT,      // 已输入部分编码
    }
}
