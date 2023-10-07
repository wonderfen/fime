/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk;

/**
 * @author zwz
 * Created on 2022-12-29
 */
@SuppressWarnings("SpellCheckingInspection")
public class Fime {

    public static final int REQUEST_READ_URI = 100;
    public static final int REQUEST_WRITE_URI = 200;
    public static final String NOTIFY_FLUTTER_SCHEMA_RESULT = "_onSchemaResult";
    public static final String NOTIFY_FLUTTER_SCHEMA_BUILD_RESULT = "_onSchemaBuildResult";
    public static final String SCHEMA_RESULT_IMPORT = "kSchemaImport";
    public static final String SCHEMA_RESULT_ACTIVE = "kSchemaActive";
    public static final String SCHEMA_RESULT_VALIDATE = "kSchemaValidate";
    public static final String SCHEMA_RESULT_BUILD = "kSchemaBuild";
    public static final String SCHEMA_RESULT_DELETE = "kSchemaDelete";

    static final String[] EXPORT_FILES = {
            "default.custom.yaml",
            "default.yaml",
            "fime_pinyin.dict.yaml",
            "fime_pinyin.schema.yaml",
            "zrm.schema.yaml",
            "punctuation.yaml",
            "symbols.yaml",
            "rime.lua",
    };

    private Fime() {
        // no instance.
    }
}
