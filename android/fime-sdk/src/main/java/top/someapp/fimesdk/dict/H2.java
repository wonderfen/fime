package top.someapp.fimesdk.dict;

import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zwz
 * Created on 2023-02-20
 */
class H2 {

    private static final String[] CREATE_SQLS = {
            "CREATE TABLE if not exists T_USER_DICT (\n"
                    + "  CODE CHARACTER VARYING NOT NULL,\n"
                    + "  TEXT CHARACTER VARYING NOT NULL,\n"
                    + "  INPUT_DATE DATE,\n"
                    + "  PRIMARY KEY (CODE,TEXT)\n"
                    + ")",
    };
    private final String id;
    private boolean started;
    private String url;
    private Connection conn;

    H2(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    void start() {
        if (started) return;
        File cacheDir = FimeContext.getInstance()
                                   .getCacheDir();
        File logFile = FimeContext.getInstance()
                                  .fileInCacheDir(id + ".trace.db");
        FileStorage.deleteFile(logFile);
        // jdbc:h2:F:\source-code\gitee\fime\android\data\fime_h2
        url = Strings.simpleFormat("jdbc:h2:%s/%s", cacheDir.getAbsolutePath(), id);
        try {
            conn = DriverManager.getConnection(url);
            for (String sql : CREATE_SQLS) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.execute();
                ps.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        started = conn != null;
    }

    void stop() {
        if (!started) return;
        try {
            conn.close();
            started = false;
            conn = null;
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            File logFile = FimeContext.getInstance()
                                      .fileInAppHome(id + ".trace.db");
            FileStorage.deleteFile(logFile);
        }
    }

    void insert(Dict.Item item) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO T_USER_DICT (CODE, TEXT, DATE) VALUES(?, ?, ?)");
            ps.setString(1, item.getCode());
            ps.setString(2, item.getText());
            ps.setDate(3, new Date(System.currentTimeMillis()));
            ps.executeUpdate();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
