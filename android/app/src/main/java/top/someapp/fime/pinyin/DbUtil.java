package top.someapp.fime.pinyin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zwz
 * Created on 2023-01-30
 */
public class DbUtil {

    private static String url = "../android/app/src/main/assets/fime.db";
    private static Connection connection;

    public static void connect() {
        connect(null);
    }

    public static void connect(String path) {
        if (connection == null) {
            try {
                Class.forName("org.sqlite.JDBC");
                String jdbcUrl = "jdbc:sqlite:";
                if (path == null || path.trim()
                                        .isEmpty()) {
                    jdbcUrl += url;
                }
                else {
                    jdbcUrl += path;
                }
                connection = DriverManager.getConnection(jdbcUrl);
            }
            catch (ClassNotFoundException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static List<Map<String, Object>> selectList(String sql, Object... params) throws SQLException {
        if (connection == null) connect();
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int count = metaData.getColumnCount();
        List<Map<String, Object>> list = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= count; i++) {  // 1 based
                String label = metaData.getColumnLabel(i);
                map.put(label, rs.getObject(label));
            }
            list.add(map);
        }
        rs.close();
        return list;
    }

    public static Map<String, Object> selectMap(String sql, Object... params) throws SQLException {
        if (connection == null) connect();
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < params.length; i++) {
            statement.setObject(i + 1, params[i]);
        }
        ResultSet rs = statement.executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        if (rs.next()) {
            int count = metaData.getColumnCount();
            Map<String, Object> map = new HashMap<>();
            for (int i = 1; i <= count; i++) {  // 1 based
                String label = metaData.getColumnLabel(i);
                map.put(label, rs.getObject(label));
            }
            return map;
        }
        rs.close();
        return Collections.EMPTY_MAP;
    }
}
