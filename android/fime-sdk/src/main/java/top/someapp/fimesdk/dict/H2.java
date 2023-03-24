package top.someapp.fimesdk.dict;

import androidx.annotation.NonNull;
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
import java.util.Collection;
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

            "CREATE TABLE IF NOT EXISTS T_DICT (\n"
                    + "  TEXT CHARACTER VARYING NOT NULL,\n"
                    + "  CODE CHARACTER VARYING NOT NULL,\n"
                    + "  WEIGHT INTEGER DEFAULT 0\n"
                    + ")",
            "CREATE INDEX IF NOT EXISTS T_DICT_CODE_IDX ON T_DICT (CODE)",
    };
    private final String id;
    private boolean started;
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
        String url = Strings.simpleFormat("jdbc:h2:%s/%s", cacheDir.getAbsolutePath(), id);
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

    List<Dict.Item> queryUserItems(String code, int limit) {
        PreparedStatement ps = prepareStatement(
                "select text from T_DICT_USER where code = ? order by hit desc limit ?",
                code,
                limit);
        List<Dict.Item> items = new ArrayList<>(limit);
        try {
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

    List<Dict.Item> queryItems(String orderBy, int start, int limit) {
        PreparedStatement ps = prepareStatement(
                "select text, code, weight from t_dict order by " + orderBy + " limit ?, ?", start,
                limit);
        List<Dict.Item> items = new ArrayList<>(limit);
        try {
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String text = rs.getString(1);
                String code = rs.getString(2);
                int weight = rs.getInt(3);
                items.add(new Dict.Item(text, code, weight));
            }
            close(rs, ps);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
        return items;
    }

    void updateUserItem(Dict.Item item) {
        try {
            PreparedStatement ps = prepareStatement(
                    "select hit from t_dict_user where code = ? and text = ?",
                    item.getCode(), item.getText());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {    // update
                int hit = rs.getInt(1);
                executeUpdate(
                        "update T_DICT_USER set hit = ?, input_date = ? where code = ? and text ="
                                + " ?",
                        true,
                        hit + 1,
                        new Date(System.currentTimeMillis()),
                        item.getCode(),
                        item.getText());
            }
            else { // insert
                executeUpdate("insert into T_DICT_USER (code, text, input_date) values(?, ?, ?)",
                              true,
                              item.getCode(),
                              item.getText(),
                              new Date(System.currentTimeMillis())
                );
            }
            close(rs, ps);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.w(e.getMessage());
        }
    }

    void clearItems() {
        executeUpdate("delete from T_DICT", true);
    }

    void dropDict() {
        executeUpdate("drop table T_DICT", true);
    }

    void insertItem(int id, @NonNull Dict.Item item, boolean commit) {
        executeUpdate("insert into T_DICT (code, text, weight) values(?, ?, ?)",
                      commit, item.getCode(), item.getText(), item.getWeight());
    }

    void insertItems(@NonNull Collection<Dict.Item> items) {
        PreparedStatement ps = prepareStatement(
                "insert into T_DICT (code, text, weight) values(?, ?, ?)");
        try {
            boolean autoCommit = conn.getAutoCommit();
            setAutoCommit(false);
            for (Dict.Item item : items) {
                ps.setString(1, item.getCode());
                ps.setString(2, item.getText());
                ps.setInt(3, item.getWeight());
                ps.executeUpdate();
            }
            commit();
            close(null, ps);
            setAutoCommit(autoCommit);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
    }

    void setAutoCommit(boolean on) {
        try {
            conn.setAutoCommit(on);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
    }

    void commit() {
        try {
            conn.commit();
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
    }

    private PreparedStatement prepareStatement(String sql, Object... params) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            for (int i = 0, len = params == null ? 0 : params.length; i < len; i++) {
                ps.setObject(i + 1, params[i]);
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
        return ps;
    }

    private void executeUpdate(String sql, boolean commit, Object... params) {
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            for (int i = 0, len = params.length; i < len; i++) {
                ps.setObject(i + 1, params[i]);
            }
            ps.executeUpdate();
            if (commit) conn.commit();
            close(null, ps);
        }
        catch (SQLException e) {
            e.printStackTrace();
            Logs.e(e.getMessage());
        }
    }

    private void close(ResultSet resultSet, Statement statement) throws SQLException {
        if (resultSet != null) resultSet.close();
        if (statement != null) statement.close();
    }
}
