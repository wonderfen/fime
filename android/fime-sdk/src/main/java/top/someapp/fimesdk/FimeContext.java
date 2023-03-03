package top.someapp.fimesdk;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author zwz
 * Created on 2022-12-23
 */
public class FimeContext {

    @SuppressLint("StaticFieldLeak")
    private static FimeContext sInstance;
    private final Application app;
    private View rootView;
    private File appHomeDir;

    public FimeContext(Application app) {
        this.app = app;
        sInstance = this;
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

    public void setRootView(View rootView) {
        this.rootView = rootView;
    }

    public void showToastShortCenter(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void showToastLongCenter(String message) {
        Toast toast = Toast.makeText(getContext(), message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public void showToastDefault(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT)
             .show();
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

    public File fileInAppHome(String path) {
        return new File(getAppHomeDir(), path);
    }

    public File fileInCacheDir(String path) {
        return new File(getCacheDir(), path);
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

    private void init() {
        File dir = getAppHomeDir();
        getCacheDir();
        if (BuildConfig.DEBUG) {
            Logs.setup(true, null);
        }
        else {
            Logs.setup(false, fileInCacheDir("fime.log"));
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
