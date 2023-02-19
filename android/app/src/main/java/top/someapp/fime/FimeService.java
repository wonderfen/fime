package top.someapp.fime;

import android.app.AppOpsManager;
import android.content.ClipDescription;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputBinding;
import android.view.inputmethod.InputConnection;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.core.view.inputmethod.InputConnectionCompat;
import androidx.core.view.inputmethod.InputContentInfoCompat;
import top.someapp.fime.view.InputView;
import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.Setting;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.engine.FimeEngine;
import top.someapp.fimesdk.engine.PinyinService;

import java.io.File;

/**
 * @author zwz
 * Created on 2022-12-20
 */
public class FimeService extends InputMethodService implements ServiceConnection {

    private static final String TAG = "FimeService";
    private static final String AUTHORITY = "com.example.android.commitcontent.ime.inputcontent";
    private ImeEngine imeService;
    private InputView inputView;

    @Override public void onCreate() {
        super.onCreate();
        Log.d(TAG, "App=" + getApplication().hashCode());
        setupEngine();
        Intent bindIntent = new Intent(this, PinyinService.class);
        bindService(bindIntent, this, Context.BIND_AUTO_CREATE);
    }

    @Override public View onCreateInputView() {
        Log.d(TAG, "onCreateInputView");
        inputView = new InputView(imeService); // 无条件创建一个新的 view，因为它不一定可以重用
        return inputView;
    }

    @Override public void onWindowShown() {
        super.onWindowShown();
        FimeContext.getInstance()
                   .setRootView(inputView);
        Log.d(TAG, "onWindowShown");
        imeService.enterState(ImeEngine.ImeState.READY);
    }

    @Override public void onWindowHidden() {
        super.onWindowHidden();
        Log.d(TAG, "onWindowHidden");
        imeService.enterState(ImeEngine.ImeState.FREEZE);
    }

    @Override public void onStartInput(EditorInfo attribute, boolean restarting) {
        super.onStartInput(attribute, restarting);
        Log.d(TAG, "onStartInput, restarting=" + restarting);
        imeService.onStartInput(attribute, restarting);
    }

    @Override public void onFinishInput() {
        super.onFinishInput();
        Log.d(TAG, "onFinishInput");
    }

    @Override public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        unbindService(this);
        imeService.enterState(ImeEngine.ImeState.QUIT);
    }

    @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
        return imeService.onKeyDown(keyCode, event);
    }

    @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
        return imeService.onKeyUp(keyCode, event);
    }

    @Override public void onServiceConnected(ComponentName name, IBinder binder) {
        // pinyinService = ((PinyinService.ServiceBinder) binder).getService();
    }

    @Override public void onServiceDisconnected(ComponentName name) {
        /*pinyinService = null;*/
    }

    private void setupEngine() {
        imeService = new FimeEngine(this);
    }

    private boolean isCommitContentSupported(
            @Nullable EditorInfo editorInfo, @NonNull String mimeType) {
        if (editorInfo == null) {
            return false;
        }

        final InputConnection ic = getCurrentInputConnection();
        if (ic == null) {
            return false;
        }

        if (!validatePackageName(editorInfo)) {
            return false;
        }

        final String[] supportedMimeTypes = EditorInfoCompat.getContentMimeTypes(editorInfo);
        for (String supportedMimeType : supportedMimeTypes) {
            if (ClipDescription.compareMimeTypes(mimeType, supportedMimeType)) {
                return true;
            }
        }
        return false;
    }

    private void doCommitContent(@NonNull String description, @NonNull String mimeType,
            @NonNull File file) {
        final EditorInfo editorInfo = getCurrentInputEditorInfo();

        // Validate packageName again just in case.
        if (!validatePackageName(editorInfo)) {
            return;
        }

        final Uri contentUri = FileProvider.getUriForFile(this, AUTHORITY, file);

        // As you as an IME author are most likely to have to implement your own content provider
        // to support CommitContent API, it is important to have a clear spec about what
        // applications are going to be allowed to access the content that your are going to share.
        final int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            // On API 25 and later devices, as an analogy of Intent.FLAG_GRANT_READ_URI_PERMISSION,
            // you can specify InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION to give
            // a temporary read access to the recipient application without exporting your content
            // provider.
            flag = InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION;
        }
        else {
            // On API 24 and prior devices, we cannot rely on
            // InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION. You as an IME author
            // need to decide what access control is needed (or not needed) for content URIs that
            // you are going to expose. This sample uses Context.grantUriPermission(), but you can
            // implement your own mechanism that satisfies your own requirements.
            flag = 0;
            try {
                // TODO: Use revokeUriPermission to revoke as needed.
                grantUriPermission(
                        editorInfo.packageName, contentUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            catch (Exception e) {
                Log.e(TAG, "grantUriPermission failed packageName=" + editorInfo.packageName
                        + " contentUri=" + contentUri, e);
            }
        }

        final InputContentInfoCompat inputContentInfoCompat = new InputContentInfoCompat(
                contentUri,
                new ClipDescription(description, new String[] { mimeType }),
                null /* linkUrl */);
        InputConnectionCompat.commitContent(
                getCurrentInputConnection(), getCurrentInputEditorInfo(), inputContentInfoCompat,
                flag, null);
    }

    private boolean validatePackageName(@Nullable EditorInfo editorInfo) {
        if (editorInfo == null) {
            return false;
        }
        final String packageName = editorInfo.packageName;
        if (packageName == null) {
            return false;
        }

        // In Android L MR-1 and prior devices, EditorInfo.packageName is not a reliable identifier
        // of the target application because:
        //   1. the system does not verify it [1]
        //   2. InputMethodManager.startInputInner() had filled EditorInfo.packageName with
        //      view.getContext().getPackageName() [2]
        // [1]: https://android.googlesource
        // .com/platform/frameworks/base/+/a0f3ad1b5aabe04d9eb1df8bad34124b826ab641
        // [2]: https://android.googlesource
        // .com/platform/frameworks/base/+/02df328f0cd12f2af87ca96ecf5819c8a3470dc8
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        }

        final InputBinding inputBinding = getCurrentInputBinding();
        if (inputBinding == null) {
            // Due to b.android.com/225029, it is possible that getCurrentInputBinding() returns
            // null even after onStartInputView() is called.
            // TODO: Come up with a way to work around this bug....
            Log.e(TAG, "inputBinding should not be null here. "
                    + "You are likely to be hitting b.android.com/225029");
            return false;
        }
        final int packageUid = inputBinding.getUid();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final AppOpsManager appOpsManager =
                    (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            try {
                appOpsManager.checkPackage(packageUid, packageName);
            }
            catch (Exception e) {
                return false;
            }
            return true;
        }

        final PackageManager packageManager = getPackageManager();
        final String possiblePackageNames[] = packageManager.getPackagesForUid(packageUid);
        for (final String possiblePackageName : possiblePackageNames) {
            if (packageName.equals(possiblePackageName)) {
                return true;
            }
        }
        return false;
    }
}
