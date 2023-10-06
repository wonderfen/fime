/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.annotation.SuppressLint;
import android.content.MutableContextWrapper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import androidx.annotation.NonNull;
import top.someapp.fime.R;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.DefaultFimeHandler;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.utils.Effects;
import top.someapp.fimesdk.utils.Jsons;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.view.Theme;
import top.someapp.fimesdk.view.VirtualKey;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * InputView 是 Android IMF(Input method framework) 要求提供给用户交互的一个 View。它必须提供的功能有：
 * 展示输入和候选界面，响应用户的操作(实体键盘事件，View上的点击，滑动等)。
 * 本类实现的逻辑有： 创建 ActionBar、Keyboards，提供绘制环境，转发用户操作、重新布局、重绘 View
 *
 * @author zwz
 * Created on 2023-04-11
 * @since 0.3.2
 */
public class InputView2 implements View.OnAttachStateChangeListener, View.OnLayoutChangeListener {

    private static final String TAG = "InputView2";
    private final ImeEngine engine;
    @SuppressWarnings("unused")
    private final Set<Theme> themes = new HashSet<>();
    private ViewGroup container;
    private ActionBarView actionBarView;
    private KeyboardView keyboardView;
    private FimeHandler painter;

    public InputView2(ImeEngine engine) {
        this.engine = engine;
        init();
        setupPainter();
    }

    public View getContainer() {
        return container;
    }

    @Override public void onViewAttachedToWindow(View v) {
    }

    @Override public void onViewDetachedFromWindow(View v) {
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        Logs.d("onLayoutChange");
    }

    public void update() {
        actionBarView.repaint();
        Map<String, Object> keyboardConfig = engine.getSchema()
                                                   .getKeyboardConfig();
        if (keyboardConfig != null) {
            keyboardView.useLayout((String) keyboardConfig.get("default-layout"));
        }
    }

    @JavascriptInterface
    @SuppressWarnings("all")
    public void jsCallNative(long id, String cmd, String args) {
        Logs.d("jsCallNative, cmd=%s", cmd);
        try {
            Map<String, Object> map = Jsons.toMap(args);
            if ("onKey".equals(cmd)) {
                onKey((String) map.get("name"));
            }
            else if ("setMode".equals(cmd)) {
                int mode = (int) map.get("mode");
                setMode(mode);
            }
            else if ("getDefaultLayout".equals(cmd)) {
                getDefaultLayout(id);
            }
        }
        catch (Exception e) {
            Logs.e(e.getMessage());
        }
    }

    FimeHandler getPainter() {
        return painter;
    }

    InputEditor getInputEditor() {
        return engine.getSchema()
                     .getInputEditor();
    }

    private void onKey(String name) {
        engine.post(Effects::playSoundAndVibrateIf);
        Keycode keycode = Keycode.getByName(name);
        if (Keycode.isAnyKeyCode(keycode.code)) {
            engine.commitText(name);
        }
        else {
            engine.onTap(new VirtualKey(keycode.code));
        }
    }

    private void setMode(int mode) {
        engine.post(Effects::playSoundAndVibrateIf);
        engine.setMode(mode);
    }

    private void getDefaultLayout(long id) {
        Map<String, Object> keyboardConfig = engine.getSchema()
                                                   .getKeyboardConfig();
        if (keyboardConfig != null) keyboardView.nativeCallJs(id, keyboardConfig);
    }

    @SuppressLint("InflateParams") private void init() {
        container = (ViewGroup) LayoutInflater.from(engine.getContext())
                                              .inflate(R.layout.intput_view, null);
        actionBarView = container.findViewById(R.id.actionBarView);
        actionBarView.setInputView(this);
        keyboardView = KeyboardView.getOrCreate(new MutableContextWrapper(engine.getContext()));
        container.addView(keyboardView);
        keyboardView.enableJsBridge(this);
        keyboardView.loadKeyboard();
        container.addOnAttachStateChangeListener(this);
        container.addOnLayoutChangeListener(this);
        FimeContext.getInstance()
                   .setRootView(container);
    }

    private void setupPainter() {
        engine.unregisterHandler(TAG + "-handler");
        painter = new DefaultFimeHandler(TAG + "-handler") {
            @Override public boolean handleOnce(@NonNull Message msg) {
                return InputView2.this.handle(msg);
            }

            @Override public void handle(@NonNull Message msg) {
                InputView2.this.handle(msg);
            }

            @Override public void send(@NonNull Message msg) {
                engine.notifyHandlers(msg);
            }
        };
        engine.registerHandler(painter);
    }

    private boolean handle(@NonNull Message msg) {
        switch (msg.what) {
            case FimeMessage.MSG_REPAINT:
            case FimeMessage.MSG_CANDIDATE_CHANGE:
            case FimeMessage.MSG_INPUT_CHANGE:
                engine.post(actionBarView::repaint);
                return true;
            case FimeMessage.MSG_CHECK_LONG_PRESS:
                Logs.d("MSG_CHECK_LONG_PRESS");
                return true;
            case FimeMessage.MSG_APPLY_THEME:
                // if (msg.obj instanceof String) applyTheme((String) msg.obj);
                return true;
        }
        Logs.d("Ignored message:0x%02x", msg.what);
        return false;
    }
}
