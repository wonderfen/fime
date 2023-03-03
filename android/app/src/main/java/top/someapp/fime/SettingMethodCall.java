package top.someapp.fime;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import io.flutter.plugin.common.MethodCall;
import top.someapp.fimesdk.Fime;
import top.someapp.fimesdk.SchemaManager;
import top.someapp.fimesdk.Setting;
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
        if ("getSchemas".equals(method)) return getSchemas();
        if ("setActiveSchema".equals(method)) return setActiveSchema(call.argument("conf"));
        if ("importExternalSchema".equals(method)) return importExternalSchema();
        if ("clearBuild".equals(method)) return clearBuild();
        if ("validateSchema".equals(method)) return validateSchema(call.argument("conf"));
        if ("buildSchema".equals(method)) return buildSchema(call.argument("conf"));
        if ("deleteSchema".equals(method)) return deleteSchema(call.argument("conf"));

        if ("getEffects".equals(method)) return getEffects();
        if ("setEffects".equals(method)) {
            boolean playKeySound = call.argument("play-key-sound");
            boolean vibrate = call.argument("vibrate");
            return setEffects(playKeySound, vibrate);
        }
        return null;
    }

    private Map<String, Object> getEffects() {
        Map<String, Object> rtn = new HashMap<>();
        rtn.put("play-key-sound", setting.getBoolean(Setting.kEffectPlayKeySound));
        rtn.put("vibrate", setting.getBoolean(Setting.kEffectVibrate));
        return rtn;
    }

    private Map<String, Object> setEffects(boolean playKeySound, boolean vibrate) {
        setting.setBoolean(Setting.kEffectPlayKeySound, playKeySound)
               .setBoolean(Setting.kEffectVibrate, vibrate);
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
