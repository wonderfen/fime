package top.someapp.fimesdk.table;

import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.defaults.DefaultEjector;

/**
 * @author zwz
 * Created on 2023-02-21
 */
@Keep
public class TableEjector extends DefaultEjector {

    private static final String TAG = "TableEjector";

    @Override public void eject(@NonNull InputEditor editor) {
        Config config = getConfig();
        if (config.hasPath("candidates")) {
            if ("eject".equals(config.getConfig("candidates")
                                     .getString("unique"))) {
                if (editor.hasCandidate() && editor.getCandidateList()
                                                   .size() == 1) {
                    Log.d(TAG, "Will eject unique candidate.");
                    commit(editor.getActiveCandidate());
                }
            }
        }
    }
}
