package top.someapp.fimesdk.table;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.defaults.DefaultEjector;
import top.someapp.fimesdk.utils.Logs;

import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-21
 */
@Keep
public class TableEjector extends DefaultEjector {

    @Override public void ejectOnCandidateChange(@NonNull InputEditor editor) {
        Config config = getConfig();
        boolean ejected = false;
        if (config.hasPath("candidates")) {
            if ("eject".equals(config.getConfig("candidates")
                                     .getString("unique"))) {
                if (editor.hasCandidate() && editor.getCandidateList()
                                                   .size() == 1) {
                    Logs.d("Will eject unique candidate.");
                    Candidate candidate = editor.getActiveCandidate();
                    commit(candidate, editor);
                    ejected = true;
                }
            }
        }
        if (!ejected && config.hasPath("code")) {
            List<? extends Config> codeConfig = config.getConfigList("code");
            String action = null;
            final String input = editor.getRawInput();
            for (Config c : codeConfig) {
                String match = c.getString("match");
                if (input.matches(match)) {
                    Logs.d(input + " matches " + match);
                    action = c.getString("action");
                    break;
                }
            }
            if ("ejectFirst".equals(action)) {
                commit(editor.getCandidateAt(0), editor);
            }
            else if ("ejectActive".equals(action)) {
                commit(editor.getActiveCandidate(), editor);
            }
            else if ("ejectSelected".equals(action)) {
                commit(editor.getSelected(), editor);
            }
        }
    }
}
