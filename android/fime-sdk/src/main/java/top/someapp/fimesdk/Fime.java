package top.someapp.fimesdk;

/**
 * @author zwz
 * Created on 2022-12-29
 */
public class Fime {

    public static final int REQUEST_READ_URI = 100;
    public static final int REQUEST_WRITE_URI = 200;
    public static final String NOTIFY_FLUTTER_SCHEMA_RESULT = "_onSchemaResult";
    public static final String SCHEMA_RESULT_IMPORT = "kSchemaImport";
    public static final String SCHEMA_RESULT_ACTIVE = "kSchemaActive";
    public static final String SCHEMA_RESULT_VALIDATE = "kSchemaValidate";
    public static final String SCHEMA_RESULT_BUILD = "kSchemaBuild";
    public static final String SCHEMA_RESULT_DELETE = "kSchemaDelete";

    static final String[] PRESET_CONF = {
            "fime_pinyin_schema.conf",
            "fime_keyboards.conf",
    };
    private static final String TAG_PREFIX = "Fime";

    private Fime() {
        // no instance.
    }

    public static String makeTag() {
        return TAG_PREFIX;
    }

    public static String makeTag(String suffix) {
        if (suffix == null || suffix.length() == 0) return TAG_PREFIX;
        return TAG_PREFIX + "/" + suffix;
    }
}
