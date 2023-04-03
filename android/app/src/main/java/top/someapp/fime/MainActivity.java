package top.someapp.fime;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import io.flutter.embedding.android.FlutterFragmentActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.embedding.engine.FlutterEngineCache;
import io.flutter.embedding.engine.dart.DartExecutor;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends FlutterFragmentActivity implements MethodChannel.MethodCallHandler {

    private static final String TAG = "MainActivity";
    private static final String kFlutterEngineId = "fime_flutter_engine";
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.VIBRATE,
            // Manifest.permission.ACCESS_WIFI_STATE,
            // Manifest.permission.ACCESS_COARSE_LOCATION,
            // Manifest.permission.ACCESS_FINE_LOCATION,
            // Manifest.permission.CAMERA,
    };
    private FlutterEngine flutterEngine;
    private MethodChannel methodChannel;
    private SettingMethodCall settingMethodCall;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
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
            result.success(Collections.EMPTY_MAP);
            callFlutter("ping", Collections.EMPTY_MAP);
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

        if (settingMethodCall == null) settingMethodCall = new SettingMethodCall(this);
        Map<String, Object> map = settingMethodCall.onMethodCall(call);
        if (map == null) {
            result.notImplemented();
        }
        else {
            result.success(map);
        }
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FimeApp app = (FimeApp) getApplication();
        app.setActivity(this);
        init();
    }

    @Override
    protected String getCachedEngineId() {
        return kFlutterEngineId;
    }

    @Override protected void onResume() {
        super.onResume();
        Logs.d("App=0x%02x", getApplication().hashCode());
        checkPermissions();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Fime.REQUEST_READ_URI) {
                Uri uriToRead = data.getData();
                Logs.d("selected uir=" + uriToRead.getPath());
                String name = Strings.afterLastChar(uriToRead.getPath(), '/');
                boolean accept = name.endsWith(".conf") || name.endsWith(".csv");
                if (accept) {
                    FileStorage.readUriToFile(this, uriToRead, FimeContext.getInstance()
                                                                          .fileInAppHome(name));
                }
                callFlutter(Fime.NOTIFY_FLUTTER_SCHEMA_RESULT,
                            SettingMethodCall.buildMessage(
                                    Fime.SCHEMA_RESULT_IMPORT,
                                    name.endsWith("_schema.conf")));
            }
            else if (requestCode == Fime.REQUEST_WRITE_URI) {
                Uri uriToWrite = data.getData();
                Logs.d("selected uir=" + uriToWrite.getPath());
                ContentResolver contentResolver = getContentResolver();
                try (OutputStream out = contentResolver.openOutputStream(uriToWrite)) {
                    out.write("export from fime\n".getBytes(StandardCharsets.UTF_8));
                    out.flush();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        for (String p : PERMISSIONS) {
            if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(this, p)) {
                permissions.add(p);
            }
        }
        if (!permissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissions.toArray(new String[0]), 0);
        }
    }

    void callFlutter(String method, Map<String, Object> params) {
        runOnUiThread(() -> {
            if (methodChannel == null) return;
            methodChannel.invokeMethod(method, params == null ? Collections.EMPTY_MAP : params);
        });
    }

    private void init() {
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
}
