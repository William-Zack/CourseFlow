package team.t508.CourseFlow.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * @author William-Zack
 */
public class DatabaseUtil {
    // 定义字符串常量作为数据库连接信息
    private static final String JDBC_URL = "jdbc:mysql://localhost/courseflow?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";
    private static final String INIT_URL = "jdbc:mysql://localhost";
    private static final String DB_NAME = "courseflow";
    private static final String USER = "root";
    private static final String PASSWORD = "123456";

    // 加载驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, USER, PASSWORD);
    }

    public static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(INIT_URL, USER, PASSWORD);
             Statement stmt = conn.createStatement()) {
            // 检测并创建数据库
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);

            // 连接到数据库
            try (Connection dbConn = getConnection();
                 Statement dbStmt = dbConn.createStatement()) {
                // 检测并创建表
                dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS students (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(50), " +
                        "password VARCHAR(50))");
                dbStmt.executeUpdate("CREATE TABLE IF NOT EXISTS teachers (" +
                        "id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "name VARCHAR(50), " +
                        "password VARCHAR(50))");
                // 插入示例数据
                dbStmt.executeUpdate("INSERT IGNORE INTO students (name, password) VALUES ('student1', 'password1')");
                dbStmt.executeUpdate("INSERT IGNORE INTO teachers (name, password) VALUES ('teacher1', 'password1')");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}