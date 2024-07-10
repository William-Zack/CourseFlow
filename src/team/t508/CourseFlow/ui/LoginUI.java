package team.t508.CourseFlow.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import team.t508.CourseFlow.utils.DatabaseUtil;

/**
 * @author William-Zack
 * @author Gavenz
 */

public class LoginUI extends JFrame {
    // 定义登录界面的组件
    private final JTextField nameField;
    private final JPasswordField passwordField;
    private boolean isTeacher;

    public LoginUI() {
        // 设置登录界面的标题、大小和关闭操作
        setTitle("欢迎使用 CourseFlow！");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板，使用BorderLayout布局
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建表单面板，使用GridLayout布局
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new JLabel("姓名:")); // 添加姓名标签
        nameField = new JTextField();
        formPanel.add(nameField); // 添加姓名输入框
        formPanel.add(new JLabel("密码:")); // 添加密码标签
        passwordField = new JPasswordField();
        formPanel.add(passwordField); // 添加密码输入框

        // 创建按钮面板，使用FlowLayout布局
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("登录");
        buttonPanel.add(loginButton); // 添加登录按钮

        // 将表单面板添加到主面板的中部
        mainPanel.add(formPanel, BorderLayout.CENTER);
        // 将按钮面板添加到主面板的底部
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // 将主面板添加到窗口
        add(mainPanel);

        // 登录按钮的事件监听
        loginButton.addActionListener(e -> {
            String name = nameField.getText();
            String password = new String(passwordField.getPassword());
            // 验证用户身份
            if (authenticateUser(name, password)) {
                JOptionPane.showMessageDialog(null, "登录成功!");
                // 打开主界面并关闭登录界面
                new MainUI(name, isTeacher).setVisible(true);
                this.dispose();
            } else {
                // 显示登录失败提示
                JOptionPane.showMessageDialog(null, "姓名或密码错误!", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private boolean authenticateUser(String name, String password) {
        // 查询数据库，验证用户身份
        String queryStudent = "SELECT * FROM students WHERE name = ? AND password = ?";
        String queryTeacher = "SELECT * FROM teachers WHERE name = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection(); // 获取数据库连接
             PreparedStatement stmtStudent = conn.prepareStatement(queryStudent);
             PreparedStatement stmtTeacher = conn.prepareStatement(queryTeacher)) {
            // 验证学生身份
            stmtStudent.setString(1, name);
            stmtStudent.setString(2, password);
            ResultSet rsStudent = stmtStudent.executeQuery();
            if (rsStudent.next()) { // 如果查询到学生记录
                isTeacher = false;
                return true;
            }

            // 验证教师身份
            stmtTeacher.setString(1, name);
            stmtTeacher.setString(2, password);
            ResultSet rsTeacher = stmtTeacher.executeQuery();
            if (rsTeacher.next()) { // 如果查询到教师记录
                isTeacher = true;
                return true;
            }

            return false;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
