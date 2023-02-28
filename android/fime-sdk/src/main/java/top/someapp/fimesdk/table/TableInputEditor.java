package top.someapp.fimesdk.table;

import androidx.annotation.Keep;
import com.typesafe.config.Config;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.defaults.DefaultInputEditor;
import top.someapp.fimesdk.utils.Strings;

import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-21
 */
@Keep
public class TableInputEditor extends DefaultInputEditor {

    private boolean canOverflow;
    private String overflowWithEmpty; // clear | accept | reject

    @Override public boolean accept(Keycode keycode) {
        if (!canOverflow || hasCandidate()) return super.accept(keycode);

        Integer codeLength = getCodeLength();
        if (codeLength == null || codeLength < 1) return super.accept(keycode);
        if (getRawInput().length() >= codeLength) {
            if ("clear".equals(overflowWithEmpty)) {
                clearInput();
                return true;
            }
            if ("reject".equals(overflowWithEmpty)) {
                return true;
            }
        }
        return super.accept(keycode);
    }

    @SuppressWarnings("unchecked")
    @Override public List<String> getSearchCodes() {
        if (canOverflow) {
            if (!hasInput()) return Collections.EMPTY_LIST;
            Integer codeLength = getCodeLength();
            if (codeLength == null || codeLength < 1 || getRawInput().length() <= codeLength) {
                return Collections.singletonList(getRawInput());
            }
            List<String> codes;
            codes = Strings.splitByLength(getRawInput(), codeLength);
            return codes;
        }
        return super.getSearchCodes();
    }

    @Override public void reconfigure(Config config) {
        super.reconfigure(config);
        if (config.hasPath("can-overflow")) {
            canOverflow = config.getBoolean("can-overflow");
        }
        if (config.hasPath("overflow-with-empty")) {
            overflowWithEmpty = config.getString("overflow-with-empty");
        }
    }
}
