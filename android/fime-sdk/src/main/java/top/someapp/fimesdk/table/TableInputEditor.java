package top.someapp.fimesdk.table;

import androidx.annotation.Keep;
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

    @SuppressWarnings("unchecked")
    @Override public List<String> getSearchCodes() {
        if (!hasInput()) return Collections.EMPTY_LIST;
        Integer codeLength = getCodeLength();
        if (codeLength == null || codeLength < 1 || getRawInput().length() <= codeLength) {
            return Collections.singletonList(getRawInput());
        }
        List<String> codes;
        codes = Strings.splitByLength(getRawInput(), codeLength);
        return codes;
    }

    @Override protected void afterAccept() {
        getEngine().eject();
    }
}
