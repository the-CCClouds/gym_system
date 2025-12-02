package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // 数据库配置信息
    private static final String URL = "jdbc:mysql://localhost:3306/gym_system?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    // 静态代码块：类加载时自动执行，只加载一次驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("✅ 数据库驱动加载成功");
        } catch (ClassNotFoundException e) {
            System.err.println("❌ 数据库驱动加载失败！");
            e.printStackTrace();
        }
    }

    /**
     * 获取数据库连接
     * @return Connection 对象
     * @throws SQLException 连接失败时抛出异常
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * 测试数据库连接
     */
    public static void testConnection() {
        try (Connection conn = getConnection()) {
            if (conn != null) {
                System.out.println("✅ 数据库连接测试成功！");
            }
        } catch (SQLException e) {
            System.err.println("❌ 数据库连接失败！");
            e.printStackTrace();
        }
    }

    // 可选：添加关闭连接的工具方法
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
