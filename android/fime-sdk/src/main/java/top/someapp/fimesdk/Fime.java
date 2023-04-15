/*
 * Copyright (c) 2023 Fime project https://fime.fit
 * Initial author: zelde126@126.com
 */

package top.someapp.fimesdk;

/**
 * @author zwz
 * Created on 2022-12-29
 */
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
            // 主题
            "default.conf",
            "default_user.conf",
            // 汉语拼音
            "pinyin_dict.csv",
            "fime_keyboards.conf",
            "fime_pinyin_schema.conf",
            // 自然码双拼
            "zrm_schema.conf",
            // 拼音93
            "pinyin93_keyboards.conf",
            "pinyin93_schema.conf",
            // 五笔画
            "stroke5_dict.csv",
            "stroke5_keyboards.conf",
            "stroke5_schema.conf",
            // 五笔86
            "wubi86_dict.csv",
            "wubi86_schema.conf",
            // 声笔飞竞
            "sbfj_dict.csv",
            "sbfj_schema.conf",
            // 自然码93
            "zrm93_keyboards.conf",
            "zrm93_schema.conf",
            // 连山易码
            "lian_shan_dict.csv",
            "lian_shan_keyboards.conf",
            "lian_shan_schema.conf",
    };

    private Fime() {
        // no instance.
    }
}
