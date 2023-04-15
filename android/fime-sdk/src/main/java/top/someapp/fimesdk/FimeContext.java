/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk;

import android.annotation.SuppressLint;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.Calendar;

/**
 * @author zwz
 * Created on 2022-12-23
 */
public class FimeContext implements Thread.UncaughtExceptionHandler {

    @SuppressLint("StaticFieldLeak")
    private static FimeContext sInstance;
    private final Application app;
    private View rootView;
    private Dialog imeDialog;
    private File appHomeDir;
    private File fatalLog;

    public FimeContext(Application app) {
        this.app = app;
        sInstance = this;
        Logs.setup(BuildConfig.DEBUG, fileInCacheDir("fime.log"));
        // Log.i("FimeContext", "setDefaultUncaughtExceptionHandler.");
        Thread.setDefaultUncaughtExceptionHandler(this);
        new Thread(this::init).start();
    }

    public static FimeContext getInstance() {
        return sInstance;
    }

    public long getInstallTime() {
        return getPackageInfo().firstInstallTime;
    }

    public long getUpdateTime() {
        return getPackageInfo().lastUpdateTime;
    }

    public Context getContext() {
        return app;
    }

    public View getRootView() {
        return rootView;
    }

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public Dialog getImeDialog() {
        return imeDialog;
    }

    public void setImeDialog(Dialog imeDialog) {
        this.imeDialog = imeDialog;
    }

    public void showToastShortCenter(String message) {
        try {
            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0); // Android 12+ setGravity() 无效
            toast.show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
    }

    public void showToastLongCenter(String message) {
        try {
            Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER, 0, 0); // Android 12+ setGravity() 无效
            toast.show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
    }

    public void showToastDefault(String message) {
        try {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT)
                 .show();
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
    }

    public AssetManager getAssets() {
        return app.getAssets();
    }

    public Resources getResources() {
        return app.getResources();
    }

    public Reader getResourcesAsReader(@RawRes int id) {
        return new InputStreamReader(getResources().openRawResource(id));
    }

    public Reader openAssetsAsReader(@NonNull String name) {
        Reader reader = null;
        try {
            reader = new InputStreamReader(getAssets().open(name));
        }
        catch (IOException e) {
            // ignored
        }
        return reader;
    }

    public InputStream openAssetsAsStream(@NonNull String name) {
        try {
            return getAssets().open(name);
        }
        catch (IOException e) {
            e.printStackTrace();
            Logs.e("openAssetsAsStream error:%s", name);
        }
        return null;
    }

    public Reader getReaderFromAppHome(String path) {
        if (FileStorage.hasFile(getAppHomeDir(), path)) {
            try {
                return new FileReader(new File(getAppHomeDir(), path));
            }
            catch (FileNotFoundException ignored) {
                Logs.e("getReaderFromAppHome error:%s", path);
            }
        }
        return null;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getAppHomeDir() {
        if (appHomeDir == null || !appHomeDir.exists()) {
            // appHomeDir = app.getFilesDir(); // /data/data/applicationId/files，开发阶段方便
            appHomeDir = app.getExternalFilesDir("");   // 查看文件方便
            appHomeDir.mkdirs();
        }
        return appHomeDir;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getCacheDir() {
        File dir = app.getExternalCacheDir();
        dir.mkdirs();
        return dir;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public File getWorkDir() {
        File dir = app.getDir("work", Context.MODE_PRIVATE);
        if (!FileStorage.hasDir(dir)) dir.mkdirs();
        return dir;
    }

    public File fileInAppHome(String path) {
        return new File(getAppHomeDir(), path);
    }

    public File fileInCacheDir(String path) {
        return new File(getCacheDir(), path);
    }

    @Override public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        StringBuilder info = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        info.append(
                Strings.simpleFormat("===\n%04d-%02d-%02dT%02d:%02d:%02d",
                                     calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                                     calendar.get(Calendar.DAY_OF_MONTH),
                                     calendar.get(Calendar.HOUR_OF_DAY),
                                     calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND)));
        info.append("\n===\nApp: ")
            .append(app.getPackageName())
            .append("/")
            .append(getPackageInfo().versionName)
            .append("\n");
        info.append("Android: ")
            .append(Build.VERSION.SDK_INT)
            .append("\n");
        info.append("Device: ")
            .append(Build.MANUFACTURER)
            .append("/")
            .append(Build.BOARD)
            .append("/")
            .append(Build.MODEL)
            .append("\n");
        info.append("===\n");
        info.append(e.getMessage())
            .append("\n");
        try (PrintWriter writer = new PrintWriter(new FileWriter(fatalLog))) {
            writer.write(info.toString());
            e.printStackTrace(writer);
            Throwable cause = e.getCause();
            if (cause != null) cause.printStackTrace(writer);
            writer.flush();
        }
        catch (IOException e2) {
            Logs.e(info.toString());
        }
    }

    PackageInfo getPackageInfo() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = app.getPackageManager()
                             .getPackageInfo(app.getPackageName(), 0);
        }
        catch (PackageManager.NameNotFoundException e) {
            // ignored
        }
        return packageInfo;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void init() {
        File dir = getAppHomeDir();
        getCacheDir();
        fatalLog = fileInAppHome("fime-fatal.log");
        try {
            if (!fatalLog.exists()) fatalLog.createNewFile();
        }
        catch (IOException e) {
            Logs.e(e.getMessage());
        }
        for (String conf : Fime.EXPORT_FILES) {
            try (InputStream ins = getAssets().open(conf)) {
                FileStorage.copyIfNotExists(ins, new File(dir, conf));  // 避免把用户修改过的文件覆盖了
            }
            catch (IOException ignored) {
            }
        }
    }
}
