/*
 * Copyright (c) 2023  Fime project https://fime.site
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.annotation.SuppressLint;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import top.someapp.fime.R;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.api.DefaultFimeHandler;
import top.someapp.fimesdk.api.FimeHandler;
import top.someapp.fimesdk.api.FimeMessage;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.view.Keyboards;
import top.someapp.fimesdk.view.Theme;

import java.util.HashSet;
import java.util.Set;

/**
 * InputView 是 Android IMF(Input method framework) 要求提供给用户交互的一个 View。它必须提供的功能有：
 * 展示输入和候选界面，响应用户的操作(实体键盘事件，View上的点击，滑动等)。
 * 本类实现的逻辑有： 创建 ActionBar、Keyboards，提供绘制环境，转发用户操作、重新布局、重绘 View
 *
 * @author zwz
 * Created on 2023-04-11
 */
public class InputView2 implements View.OnAttachStateChangeListener {

    private static final String TAG = "InputView2";
    private final ImeEngine engine;
    @SuppressWarnings("unused")
    private final Set<Theme> themes = new HashSet<>();
    private View container;
    private ActionBarView actionBarView;
    private WebView webView;
    @SuppressWarnings("unused")
    private Keyboards keyboards;
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
        WebSettings webViewSettings = webView.getSettings();
        webViewSettings.setJavaScriptEnabled(true);
        webViewSettings.setUseWideViewPort(true);
        webViewSettings.setDisplayZoomControls(false);
        webViewSettings.setAllowFileAccess(true);
        webViewSettings.setDomStorageEnabled(true);
        webViewSettings.setDatabaseEnabled(true);
        webViewSettings.setAppCacheEnabled(true);
        webViewSettings.setLoadsImagesAutomatically(true);
        webViewSettings.setDefaultTextEncodingName("utf-8");
        webView.loadUrl("file:///android_asset/keyboards/qwerty.html");
    }

    @Override public void onViewDetachedFromWindow(View v) {

    }

    FimeHandler getPainter() {
        return painter;
    }

    InputEditor getInputEditor() {
        return engine.getSchema()
                     .getInputEditor();
    }

    @SuppressLint("InflateParams") private void init() {
        container = LayoutInflater.from(engine.getContext())
                                  .inflate(R.layout.intput_view, null);
        actionBarView = container.findViewById(R.id.actionBarView);
        actionBarView.setInputView(this);
        webView = container.findViewById(R.id.webview);
        container.addOnAttachStateChangeListener(this);
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
