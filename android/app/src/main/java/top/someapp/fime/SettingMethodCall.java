package top.someapp.fime;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import io.flutter.plugin.common.MethodCall;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.SchemaManager;
import top.someapp.fimesdk.Setting;
import top.someapp.fimesdk.utils.Clipboard;
import top.someapp.fimesdk.utils.Logs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-01-17
 */
@SuppressWarnings("unchecked")
class SettingMethodCall {

    private final Activity activity;
    private final Setting setting;

    SettingMethodCall(Activity activity) {
        this.activity = activity;
        setting = Setting.getInstance();
    }

    static Map<String, Object> buildMessage(String key, Object value) {
        Map<String, Object> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    Map<String, Object> onMethodCall(@NonNull MethodCall call) {
        final String method = call.method;
        Logs.d("onMethodCall %s", method);
        if ("getSchemas".equals(method)) return getSchemas();
        if ("setActiveSchema".equals(method)) return setActiveSchema(call.argument("conf"));
        if ("importExternalSchema".equals(method)) return importExternalSchema();
        if ("clearBuild".equals(method)) return clearBuild();
        if ("validateSchema".equals(method)) return validateSchema(call.argument("conf"));
        if ("buildSchema".equals(method)) return buildSchema(call.argument("conf"));
        if ("deleteSchema".equals(method)) return deleteSchema(call.argument("conf"));

        if ("getKeyboardSetting".equals(method)) return getKeyboardSetting();
        if ("setKeyboardSetting".equals(method)) {
            boolean playKeySound = call.argument(Setting.kKeyboardPlayKeySound);
            boolean keyVibrate = call.argument(Setting.kKeyboardKeyVibrate);
            boolean checkLongPress = call.argument(Setting.kKeyboardCheckLongPress);
            boolean checkSwipe = call.argument(Setting.kKeyboardCheckSwipe);
            return setKeyboardSetting(playKeySound, keyVibrate, checkLongPress, checkSwipe);
        }

        if ("getThemeSetting".equals(method)) return getThemeSetting();
        if ("setThemeSetting".equals(method)) return setThemeSetting(call.argument(Setting.kTheme));

        if ("getClipboardSetting".equals(method)) return getClipboardSetting();
        if ("setClipboardSetting".equals(method)) {
            return setClipboardSetting(call.argument(Setting.kClipboardEnabled));
        }
        if ("cleanClipboard".equals(method)) return cleanClipboard();
        return null;
    }

    private Map<String, Object> getKeyboardSetting() {
        Map<String, Object> rtn = new HashMap<>();
        rtn.put(Setting.kKeyboardPlayKeySound, setting.getBoolean(Setting.kKeyboardPlayKeySound));
        rtn.put(Setting.kKeyboardKeyVibrate, setting.getBoolean(Setting.kKeyboardKeyVibrate));
        rtn.put(Setting.kKeyboardCheckLongPress,
                setting.getBoolean(Setting.kKeyboardCheckLongPress));
        rtn.put(Setting.kKeyboardCheckSwipe, setting.getBoolean(Setting.kKeyboardCheckSwipe));
        return rtn;
    }

    private Map<String, Object> setKeyboardSetting(boolean playKeySound, boolean keyVibrate,
            boolean checkLongPress, boolean checkSwipe) {
        setting.setBoolean(Setting.kKeyboardPlayKeySound, playKeySound)
               .setBoolean(Setting.kKeyboardKeyVibrate, keyVibrate)
               .setBoolean(Setting.kKeyboardCheckLongPress, checkLongPress)
               .setBoolean(Setting.kKeyboardCheckSwipe, checkSwipe);
        return Collections.EMPTY_MAP;
    }

    private Map<String, Object> getThemeSetting() {
        Map<String, Object> rtn = new HashMap<>();
        rtn.put(Setting.kTheme, setting.getString(Setting.kTheme));
        return rtn;
    }

    private Map<String, Object> setThemeSetting(String theme) {
        setting.setString(Setting.kTheme, theme);
        return Collections.EMPTY_MAP;
    }

    private Map<String, Object> getClipboardSetting() {
        Map<String, Object> rtn = new HashMap<>();
        rtn.put(Setting.kClipboardEnabled, setting.getBoolean(Setting.kClipboardEnabled));
        return rtn;
    }

    private Map<String, Object> setClipboardSetting(boolean clipboardEnabled) {
        setting.setBoolean(Setting.kClipboardEnabled, clipboardEnabled);
        return Collections.EMPTY_MAP;
    }

    private Map<String, Object> cleanClipboard() {
        Clipboard.clean();
        return Collections.EMPTY_MAP;
    }

    private Map<String, Object> getSchemas() {
        Map<String, Object> rtn = new HashMap<>();
        List<Map<String, Object>> schemas = new ArrayList<>();
        try {
            for (SchemaManager.SchemaInfo info : SchemaManager.scan()) {
                Map<String, Object> map = new HashMap<>(3);
                map.put("conf", info.conf);
                map.put("precompiled", info.precompiled);
                map.put("name", info.getName());
                schemas.add(map);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            Logs.e("getSchemas error:%s", e.getMessage());
        }
        rtn.put("schemas", schemas);
        rtn.put("active", setting.getString(Setting.kActiveSchema));
        return rtn;
    }

    private Map<String, Object> setActiveSchema(String conf) {
        setting.setString(Setting.kActiveSchema, conf)
               .save();
        return buildMessage("success", true);
    }

    private Map<String, Object> importExternalSchema() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*")
              .addCategory(Intent.CATEGORY_OPENABLE)
              // .putExtra(Intent.EXTRA_MIME_TYPES, kMimeTypes)
              .putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        activity.startActivityForResult(Intent.createChooser(intent, "选择文件"),
                                        Fime.REQUEST_READ_URI);
        return Collections.EMPTY_MAP;
    }

    private Map<String, Object> clearBuild() {
        SchemaManager.clearBuild();
        return buildMessage("success", true);
    }

    private Map<String, Object> deleteSchema(String conf) {
        SchemaManager.delete(conf);
        return buildMessage("success", true);
    }

    private Map<String, Object> buildSchema(String conf) {
        return buildMessage("success", SchemaManager.build(conf));
    }

    private Map<String, Object> validateSchema(String conf) {
        if (SchemaManager.validate(conf)) return buildMessage("success", true);
        return Collections.EMPTY_MAP;
    }
}
