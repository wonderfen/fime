package top.someapp.fime;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import io.flutter.embedding.android.FlutterFragmentActivity;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Strings;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends FlutterFragmentActivity {

    private static final String TAG = Fime.makeTag("MainActivity");
    private static final String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.CAMERA,
    };
    private long backPressedTime;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
    }

    @Override public void onBackPressed() {
        long now = System.currentTimeMillis();
        if (backPressedTime > 0 && now - backPressedTime <= 1200) {
            super.onBackPressed();
        }
        else {
            backPressedTime = now;
            FimeContext.getInstance()
                       .showToastDefault("再次点击 返回 退出应用!");
        }
    }

    @Override protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FimeApp app = (FimeApp) getApplication();
        app.setActivity(this);
    }

    @Override
    protected String getCachedEngineId() {
        return FimeApp.kFlutterEngineId;
    }

    @Override protected void onResume() {
        super.onResume();
        Log.d(TAG, "App=" + getApplication().hashCode());
        // 申请权限
        // Build.VERSION_CODES.S // Android 12
        checkPermissions();
        // if (uriToRead == null) { // read
        //     Intent intent = new Intent(Intent.ACTION_GET_CONTENT).setType("*/*")
        //                                                          .addCategory(
        //                                                                  Intent
        //                                                                  .CATEGORY_OPENABLE);
        //     startActivityForResult(Intent.createChooser(intent, "选择文件"), REQUEST_READ_URI);
        // }
        // else { // write
        //     Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT).setType(
        //             "application/octet-stream");
        //     intent.addCategory(Intent.CATEGORY_OPENABLE)
        //           .putExtra(Intent.EXTRA_TITLE, "fime-export.txt");
        //     startActivityForResult(Intent.createChooser(intent, "选择导出位置"), REQUEST_WRITE_URI);
        // }
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Fime.REQUEST_READ_URI) {
                Uri uriToRead = data.getData();
                Log.d(TAG, "selected uir=" + uriToRead.getPath());
                String name = Strings.afterLastChar(uriToRead.getPath(), '/');
                boolean accept = name.endsWith(".conf") || name.endsWith(".csv");
                if (accept) {
                    FileStorage.readUriToFile(this, uriToRead, FimeContext.getInstance()
                                                                          .fileInAppHome(name));
                }
                ((FimeApp) getApplication()).callFlutter(Fime.NOTIFY_FLUTTER_SCHEMA_RESULT,
                                                         SettingMethodCall.buildMessage(
                                                                 Fime.SCHEMA_RESULT_IMPORT,
                                                                 name.endsWith("_schema.conf")));
            }
            else if (requestCode == Fime.REQUEST_WRITE_URI) {
                Uri uriToWrite = data.getData();
                Log.d(TAG, "selected uir=" + uriToWrite.getPath());
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
}
