package top.someapp.fimesdk.dict;

import top.someapp.fimesdk.FimeContext;
import top.someapp.fimesdk.utils.FileStorage;
import top.someapp.fimesdk.utils.Logs;
import top.someapp.fimesdk.utils.Strings;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zwz
 * Created on 2023-02-20
 */
class H2 {

    private static final String[] CREATE_SQLS = {
            "CREATE TABLE IF NOT EXISTS T_DICT_USER (\n"
                    + "  CODE CHARACTER VARYING,\n"
                    + "  TEXT CHARACTER VARYING,\n"
                    + "  HIT INTEGER DEFAULT 0,\n"
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
                ps.executeUpdate();
                ps.close();
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
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
            Logs.w(e.getMessage());
        }
        finally {
            File logFile = FimeContext.getInstance()
                                      .fileInAppHome(id + ".trace.db");
            FileStorage.deleteFile(logFile);
        }
    }

    List<Dict.Item> query(String code, int limit) {
        List<Dict.Item> items = new ArrayList<>(limit);
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "select text from T_DICT_USER where code = ? order by hit desc");
            ps.setString(1, code);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String text = rs.getString(1);
                items.add(new Dict.Item(text, code));
            }
            close(rs, ps);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
        return items;
    }

    void insertOrUpdate(Dict.Item item) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                    "select hit from t_dict_user where code = ? and text = ?");
            ps.setString(1, item.getCode());
            ps.setString(2, item.getText());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    // update
                int hit = rs.getInt(1);
                close(rs, ps);
                ps = conn.prepareStatement(
                        "update T_DICT_USER set hit = ?, input_date = ? where code = ? and text ="
                                + " ?");
                ps.setInt(1, hit + 1);
                ps.setDate(2, new Date(System.currentTimeMillis()));
                ps.setString(3, item.getCode());
                ps.setString(4, item.getText());
            }
            else { // insert
                close(rs, ps);
                ps = conn.prepareStatement(
                        "insert into T_DICT_USER (code, text, input_date) values(?, ?, ?)");
                ps.setString(1, item.getCode());
                ps.setString(2, item.getText());
                ps.setDate(3, new Date(System.currentTimeMillis()));
            }
            ps.executeUpdate();
            close(null, ps);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
    }

    private void close(ResultSet resultSet, Statement statement) throws SQLException {
        if (resultSet != null) resultSet.close();
        if (statement != null) statement.close();
    }
}
