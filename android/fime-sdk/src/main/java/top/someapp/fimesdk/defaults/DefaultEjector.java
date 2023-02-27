package top.someapp.fimesdk.defaults;

import androidx.annotation.NonNull;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Candidate;
import top.someapp.fimesdk.api.Ejector;
import top.someapp.fimesdk.api.ImeEngine;
import top.someapp.fimesdk.api.InputEditor;

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

    @Override public void eject(@NonNull InputEditor editor) {
        if (editor.hasInput()) {
            Candidate candidate = new Candidate("", "");
            if (editor.getSelected() != null) {
                candidate = candidate.append(editor.getSelected());
            }
            if (editor.getActiveCandidate() != null) {
                candidate = candidate.append(editor.getActiveCandidate());
            }
            commit(candidate);
        }
    }

    protected ImeEngine getEngine() {
        return engine;
    }

    protected void commit(Candidate candidate) {
        if (engine == null || candidate.text.isEmpty()) return;
        engine.commitText(candidate.text);
        try {
            engine.post(() -> engine.getSchema()
                                    .getTranslator()
                                    .updateDict(candidate));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
