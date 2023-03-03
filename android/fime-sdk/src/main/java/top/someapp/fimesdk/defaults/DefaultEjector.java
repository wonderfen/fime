package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.Ejector;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;
import top.someapp.fimesdk.utils.Logs;

/**
 * @author zwz
 * Created on 2023-02-07
 */
public class DefaultEjector implements Ejector {

    private ImeEngine engine;
    private Config config;

    @Override public Config getConfig() {
        return config;
    }

    @Override public void reconfigure(Config config) {
        this.config = config;
    }

    @Override public void setup(@NonNull ImeEngine engine) {
        this.engine = engine;
    }

    @Override public void manualEject(@NonNull InputEditor editor) {
        if (editor.hasInput()) {
            Candidate candidate = new Candidate("", "");
            if (editor.getSelected() != null) {
                candidate = candidate.append(editor.getSelected());
            }
            if (editor.getActiveCandidate() != null) {
                candidate = candidate.append(editor.getActiveCandidate());
            }
            commit(candidate, editor);
        }
    }

    @Override public void ejectOnCandidateChange(@NonNull InputEditor editor) {

    }

    protected ImeEngine getEngine() {
        return engine;
    }

    protected void commit(Candidate candidate, InputEditor editor) {
        if (engine == null || candidate.text.isEmpty()) return;
        String remains = null;
        if (editor.getCursor() > candidate.code.length()) {
            remains = editor.getRawInput()
                            .substring(candidate.code.length());
        }
        engine.commitText(candidate.text);
        if (remains != null) {
            editor.append(remains);
            engine.requestSearch();
        }
        try {
            engine.post(() -> engine.getSchema()
                                    .getTranslator()
                                    .updateDict(candidate));
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
    }
}
