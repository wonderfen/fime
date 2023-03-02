package top.someapp.fimesdk.table;

import androidx.annotation.Keep;
import com.typesafe.config.Config;
import top.someapp.fimesdk.api.Syncopate;
import top.someapp.fimesdk.config.Keycode;
import top.someapp.fimesdk.defaults.DefaultInputEditor;
import top.someapp.fimesdk.utils.Strings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

/**
 * @author zwz
 * Created on 2023-02-21
 */
@Keep
public class TableInputEditor extends DefaultInputEditor {

    private Stack<String> searchCodes = new Stack<>();
    private String lastSegment = "";
    private boolean canOverflow;
    private String overflowWithEmpty; // clear | accept | reject

    @Override public boolean accept(Keycode keycode) {
        // if (!canOverflow || hasCandidate()) return super.accept(keycode);
        //
        // Integer codeLength = getCodeLength();
        // if (codeLength == null || codeLength < 1) return super.accept(keycode);
        // if (getRawInput().length() >= codeLength) {
        //     if ("clear".equals(overflowWithEmpty)) {
        //         clearInput();
        //         return true;
        //     }
        //     if ("reject".equals(overflowWithEmpty)) {
        //         return true;
        //     }
        // }
        return super.accept(keycode);
    }

    @SuppressWarnings("unchecked")
    @Override public List<String> getSearchCodes() {
        lastSegment = "";
        if (hasInput()) {
            List<String> codes = segments();    // 分段
            while (!searchCodes.isEmpty() && searchCodes.size() >= codes.size()) {
                searchCodes.pop();
            }
            if (codes.isEmpty()) {
                searchCodes.push("");
            }
            else {
                lastSegment = codes.get(codes.size() - 1);
                searchCodes.push(getConverter().convert(lastSegment));
            }
            return searchCodes;
        }
        return Collections.EMPTY_LIST;
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

    private List<String> segments() {
        final Syncopate syncopate = getSyncopate();
        List<String> groups = new ArrayList<>();
        char delimiter = getDelimiter();
        String remains;
        if (delimiter > '\0') {
            remains = syncopate.segments(getRawInput(), groups, delimiter);
        }
        else {
            remains = syncopate.segments(getRawInput(), groups);
        }
        if (!Strings.isNullOrEmpty(remains)) groups.add(remains);
        return groups;
    }
}
