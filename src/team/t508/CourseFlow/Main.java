package team.t508.CourseFlow;

import team.t508.CourseFlow.ui.LoginUI;
import team.t508.CourseFlow.utils.DatabaseUtil;

import javax.swing.*;

/**
 * @author William-Zack
 */
public class Main {
    public static void main(String[] args) {
        // 初始化数据库
        DatabaseUtil.initializeDatabase();

        // 显示登录界面
        SwingUtilities.invokeLater(() -> new LoginUI().setVisible(true));
    }
}
