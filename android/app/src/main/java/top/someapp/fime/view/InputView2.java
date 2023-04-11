/*
 * Copyright (c) 2023  Fime project https://fime.site
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import top.someapp.fime.R;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.view.Keyboards;
import top.someapp.fimesdk.view.Theme;

import java.util.HashSet;
import java.util.Set;

/**
 * @author zwz
 * Created on 2023-04-11
 */
public class InputView2 implements View.OnAttachStateChangeListener {

    private static final String TAG = "InputView2";
    private static boolean drawPath;
    private final ImeEngine engine;
    private final Set<Theme> themes = new HashSet<>();
    private View container;
    private ActionBar2 actionBar;
    private WebView webView;
    private Keyboards keyboards;

    public InputView2(ImeEngine engine) {
        this.engine = engine;
        init();
    }

    public View getContainer() {
        return container;
    }

    @Override public void onViewAttachedToWindow(View v) {
        if (webView != null) {
            webView.loadUrl("file:///android_asset/index.html");
        }
    }

    @Override public void onViewDetachedFromWindow(View v) {

    }

    private void init() {
        container = LayoutInflater.from(engine.getContext())
                                  .inflate(R.layout.intput_view, null);
        actionBar = container.findViewById(R.id.actionBar);
        webView = container.findViewById(R.id.webview);
        container.addOnAttachStateChangeListener(this);
    }
}
