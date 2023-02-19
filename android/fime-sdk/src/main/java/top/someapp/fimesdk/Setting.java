package top.someapp.fimesdk;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author zwz
 * Create on 2023-01-15
 */
@SuppressWarnings("unchecked")
public class Setting {

    // public static final String kSchemas = "schemas";            // 已知的方案
    public static final String kActiveSchema = "active-schema";  // 用户激活的方案
    public static final String kEffectPlayKeySound = "effect.play-key-sound";
    public static final String kEffectVibrate = "effect.vibrate";
    private static Setting sInstance;
    private final SharedPreferences pref;

    private Setting() {
        Context context = FimeContext.getInstance()
                                     .getContext();
        pref = context.getSharedPreferences("fime_pref", Context.MODE_PRIVATE);
        init();
    }

    public static Setting getInstance() {
        if (sInstance == null) sInstance = new Setting();
        return sInstance;
    }

    public void save() {
        pref.edit()
            .apply();
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String defaultValue) {
        if (pref.contains(key)) {
            return pref.getString(key, defaultValue);
        }
        pref.edit()
            .putString(key, defaultValue)
            .apply();
        return defaultValue;
    }

    public Setting setString(String key, String value) {
        pref.edit()
            .putString(key, value)
            .apply();
        return this;
    }

    public List<String> getStringList(String key) {
        if (pref.contains(key)) {
            return Arrays.asList(pref.getString(key, "")
                                     .split(","));
        }
        return Collections.EMPTY_LIST;
    }

    public Setting setStringList(String key, List<String> value) {
        StringBuilder sb = new StringBuilder();
        for (String v : value) {
            sb.append(",")
              .append(v);
        }
        setString(key, sb.substring(1));
        return this;
    }

    public boolean getBoolean(String key) {
        return pref.contains(key) && pref.getBoolean(key, false);
    }

    public Setting setBoolean(String key, boolean on) {
        pref.edit()
            .putBoolean(key, on)
            .apply();
        return this;
    }

    private void init() {
        if (pref.getAll()
                .isEmpty()) {
            pref.edit()
                // .putString(kSchemas, "fime_pinyin_schema.conf,")
                .putString(kActiveSchema, "fime_pinyin_schema.conf")
                .putBoolean(kEffectPlayKeySound, true)
                .putBoolean(kEffectVibrate, false)
                .apply();
        }
    }
}
