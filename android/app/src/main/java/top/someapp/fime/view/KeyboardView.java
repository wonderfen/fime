/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fime.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
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
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-04-17
 * @since 0.3.2
 */
public class KeyboardView extends WebView {

    private static final String kAssetsPrefix = "file:///android_asset/keyboards/";
    private static String[] assetsInKeyboards;

    public KeyboardView(@NonNull Context context) {
        super(context);
        init();
    }

    public KeyboardView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
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
