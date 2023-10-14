/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.MutableContextWrapper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.utils.Jsons;
import top.someapp.fimesdk.utils.Logs;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-04-17
 * @since 0.3.2
 */
public class KeyboardView extends WebView implements View.OnLayoutChangeListener {

    private static final String kAssetsPrefix = "file:///android_asset/keyboards/";
    private static String[] assetsInKeyboards;
    private static KeyboardView sInstance;

    public KeyboardView(@NonNull Context context) {
        super(context);
        init();
    }

    public KeyboardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static KeyboardView getOrCreate(@NonNull MutableContextWrapper context) {
        if (sInstance == null || !(sInstance.getContext() instanceof MutableContextWrapper)) {
            Logs.d("create KeyboardView.");
            sInstance = new KeyboardView(context);
        }
        else {
            ViewParent parent = sInstance.getParent();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(sInstance);
            }
            ((MutableContextWrapper) sInstance.getContext()).setBaseContext(
                    context.getBaseContext());
        }
        return sInstance;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft,
            int oldTop, int oldRight, int oldBottom) {
        Logs.d("onLayoutChange, dx: %d, dy: %d", right - oldLeft,
               bottom - oldTop);
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Logs.i("onMeasure");
    }

    @SuppressLint("JavascriptInterface") void enableJsBridge(@NonNull Object jsBridge) {
        addJavascriptInterface(jsBridge, "android");
    }

    void loadKeyboard() {
        loadKeyboard("keyboards/index.html", "keyboards/keyboard.html", "/keyboard.html");
    }

    void loadKeyboard(@NonNull String... urls) {
        boolean loaded;
        for (String url : urls) {
            if (url.charAt(0) == '/') {
                loaded = loadFileInAssets(url.substring(1));
            }
            else {
                loaded = loadFileInAppHome(url);
            }
            if (loaded) break;
        }
    }

    void nativeCallJs(long id, Map<String, Object> args) {
        evalScript("fime.nativeCallback", id, Jsons.toJSONString(args, "{}"));
    }

    @SuppressWarnings("SpellCheckingInspection")
    void useLayout(String layout) {
        if (layout != null) {
            evalScript("fime.onNativeCall", "'onKeyboardLayout'", "{layout:'" + layout + "'}");
        }
    }

    void onInputChange(@NonNull String composition, @NonNull List<String> candidates) {
        Map<String, Object> args = new HashMap<>();
        args.put("composition", composition);
        args.put("candidates", candidates);
        evalScript("fime.onNativeCall", "'onInputChange'", Jsons.toJSONString(args));
    }

    private void evalScript(@NonNull String function, Object... args) {
        StringBuilder script = new StringBuilder(function);
        if (args == null || args.length == 0) {
            script.append("()");
        }
        else {
            boolean first = true;
            script.append("(");
            for (Object o : args) {
                if (first) {
                    first = false;
                }
                else {
                    script.append(",");
                }
                script.append(o);
            }
            script.append(")");
        }
        post(() -> evaluateJavascript(script.toString(), null));
    }

    @SuppressLint("SetJavaScriptEnabled") private void init() {
        clearCache(true);

        WebSettings s = getSettings();
        s.setJavaScriptEnabled(true);
        s.setUseWideViewPort(true);
        s.setDisplayZoomControls(false);
        s.setAllowFileAccess(true);
        s.setDomStorageEnabled(true);
        s.setDatabaseEnabled(true);
        s.setAppCacheEnabled(true);
        s.setLoadsImagesAutomatically(true);
        //noinspection deprecation
        s.setSavePassword(false);
        s.setSaveFormData(false);
        s.setAllowUniversalAccessFromFileURLs(true);
        s.setSupportZoom(false);
        s.setDomStorageEnabled(true);
        s.setTextZoom(100);   // 处理系统设置字体大小对应用的影响，如miui
        s.setDefaultTextEncodingName("utf-8");

        if (assetsInKeyboards == null) {
            try {
                assetsInKeyboards = getContext().getAssets()
                                                .list("keyboards");
            }
            catch (IOException ignored) {
                Logs.w("No assets found in keyboards!");
            }
        }
        addOnLayoutChangeListener(this);
    }

    private boolean loadFileInAppHome(String name) {
        File file = FimeContext.getInstance()
                               .fileInAppHome(name);
        if (file.exists()) {
            super.loadUrl("file://" + file.getAbsolutePath());
            return true;
        }
        return false;
    }

    private boolean loadFileInAssets(String name) {
        if (assetsInKeyboards != null && Arrays.binarySearch(assetsInKeyboards, name) >= 0) {
            super.loadUrl(kAssetsPrefix + name);
            return true;
        }
        return false;
    }
}
