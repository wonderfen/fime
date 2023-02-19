package top.someapp.fimesdk.engine;

import android.app.Service;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.os.IBinder;
import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.SearchService;

import java.io.FileDescriptor;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Create on 2023-01-03
 */
@Keep
public class PinyinService extends Service implements SearchService {

    private final static int MAX_PATH_FILE_LENGTH = 100;
    private static boolean inited = false;

    static {
        try {
            System.loadLibrary("jni_pinyinime");
        }
        catch (UnsatisfiedLinkError ule) {
            Log.e("PinyinDecoderService",
                  "WARNING: Could not load jni_pinyinime natives");
        }
    }

    private IBinder binder = new SearchService.ServiceBinder(this);
    private String mUsr_dict_file;

    native static boolean nativeImOpenDecoder(byte fn_sys_dict[],
            byte fn_usr_dict[]);

    native static boolean nativeImOpenDecoderFd(FileDescriptor fd,
            long startOffset, long length, byte fn_usr_dict[]);

    native static void nativeImSetMaxLens(int maxSpsLen, int maxHzsLen);

    native static boolean nativeImCloseDecoder();

    native static int nativeImSearch(byte pyBuf[], int pyLen);

    native static int nativeImDelSearch(int pos, boolean is_pos_in_splid,
            boolean clear_fixed_this_step);

    native static void nativeImResetSearch();

    native static int nativeImAddLetter(byte ch);

    native static String nativeImGetPyStr(boolean decoded);

    native static int nativeImGetPyStrLen(boolean decoded);

    native static int[] nativeImGetSplStart();

    native static String nativeImGetChoice(int choiceId);

    native static int nativeImChoose(int choiceId);

    native static int nativeImCancelLastChoice();

    native static int nativeImGetFixedLen();

    native static boolean nativeImCancelInput();

    native static boolean nativeImFlushCache();

    native static int nativeImGetPredictsNum(String fixedStr);

    native static String nativeImGetPredictItem(int predictNo);

    // Sync related
    native static String nativeSyncUserDict(byte[] user_dict, String tomerge);

    native static boolean nativeSyncBegin(byte[] user_dict);

    native static boolean nativeSyncFinish();

    native static String nativeSyncGetLemmas();

    native static int nativeSyncPutLemmas(String tomerge);

    native static int nativeSyncGetLastCount();

    native static int nativeSyncGetTotalCount();

    native static boolean nativeSyncClearLastGot();

    native static int nativeSyncGetCapacity();

    @Override
    public void onCreate() {
        super.onCreate();
        mUsr_dict_file = getFileStreamPath("usr_dict.dat").getPath();
        // This is a hack to make sure our "files" directory has been
        // created.
        try {
            // ensure files dir exists
            openFileOutput("dummy", 0).close();
        }
        catch (IOException e) {
        }

        initPinyinEngine();
    }

    @Override
    public void onDestroy() {
        nativeImCloseDecoder();
        inited = false;
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override public boolean isAlive() {
        return inited;
    }

    @Override public List<Candidate> search(String code, int limit) {
        int count = nativeImSearch(code.getBytes(StandardCharsets.UTF_8), code.length());
        List<Candidate> candidateList = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String s = nativeImGetChoice(i);
            candidateList.add(new Candidate(code, s));
        }
        nativeImResetSearch();
        return candidateList;
    }

    @Override public void stop() {
        stopSelf();
    }

    // Get file name of the specified dictionary
    private boolean getUsrDictFileName(byte usr_dict[]) {
        if (null == usr_dict) {
            return false;
        }

        for (int i = 0; i < mUsr_dict_file.length(); i++) {
            usr_dict[i] = (byte) mUsr_dict_file.charAt(i);
        }
        usr_dict[mUsr_dict_file.length()] = 0;

        return true;
    }

    private void initPinyinEngine() {
        byte usr_dict[];
        usr_dict = new byte[MAX_PATH_FILE_LENGTH];

        // Here is how we open a built-in dictionary for access through
        // a file descriptor...
        try {
            AssetFileDescriptor afd = getAssets().openFd("dict_pinyin.dat");
            if (getUsrDictFileName(usr_dict)) {
                inited = nativeImOpenDecoderFd(afd.getFileDescriptor(), afd
                        .getStartOffset(), afd.getLength(), usr_dict);
            }
            afd.close();
        }
        catch (IOException e) {
            Log.e("Fime/PinyinService", e.toString());
        }
    }
}
