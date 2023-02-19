package top.someapp.fime;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import top.someapp.fimesdk.FimeContext;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zwz
 * Created on 2022-12-20
 */
public class FimeApp extends Application implements MethodChannel.MethodCallHandler {

    static final String kFlutterEngineId = "fime_flutter_engine";
    private static final Map<String, Object> kEmptyMap = Collections.EMPTY_MAP;
    private FlutterEngine flutterEngine;
    private MethodChannel methodChannel;
    private SettingMethodCall settingMethodCall;
    private Activity activity;

    @Override
    public void onCreate() {
        super.onCreate();
        new FimeContext(this);
        AppDatabase.getInstance(this);
        // Instantiate a FlutterEngine.
        flutterEngine = new FlutterEngine(this);

        // Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.getDartExecutor()
                     .executeDartEntrypoint(DartExecutor.DartEntrypoint.createDefault());

        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache.getInstance()
                          .put(kFlutterEngineId, flutterEngine);

        // setup methodChannel
        methodChannel = new MethodChannel(flutterEngine.getDartExecutor()
                                                       .getBinaryMessenger(), "FimeApp");
        methodChannel.setMethodCallHandler(this);
    }

    public Activity getActivity() {
        return activity;
    }

    void setActivity(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onMethodCall(@NonNull MethodCall call, @NonNull MethodChannel.Result result) {
        if ("switchToFime".equals(call.method)) {
            InputMethodManager imm = (InputMethodManager) getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            boolean enabled = false;
            final String selfPackageName = getPackageName();
            for (InputMethodInfo imi : imm.getEnabledInputMethodList()) {
                if (selfPackageName.equals(imi.getPackageName())) {
                    enabled = true;
                    break;
                }
            }
            if (enabled) {
                imm.showInputMethodPicker();
            }
            else {
                Intent intent = new Intent(Settings.ACTION_INPUT_METHOD_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            result.success(kEmptyMap);
            callFlutter("ping", kEmptyMap);
            return;
        }

        if ("versionInfo".equals(call.method)) {
            Map<String, Object> versionInfo = new HashMap<>();
            versionInfo.put("debug", BuildConfig.DEBUG);
            versionInfo.put("versionCode", BuildConfig.VERSION_CODE);
            versionInfo.put("versionName", BuildConfig.VERSION_NAME);
            result.success(versionInfo);
            return;
        }

        if (settingMethodCall == null) settingMethodCall = new SettingMethodCall(getActivity());
        Map<String, Object> map = settingMethodCall.onMethodCall(call);
        if (map == null) {
            result.notImplemented();
        }
        else {
            result.success(map);
        }
    }

    void callFlutter(String method, Map<String, Object> params) {
        if (methodChannel == null) return;
        methodChannel.invokeMethod(method, params == null ? kEmptyMap : params);
    }
}

