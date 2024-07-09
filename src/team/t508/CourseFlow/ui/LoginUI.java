package team.t508.CourseFlow.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import team.t508.CourseFlow.utils.DatabaseUtil;

/**
 * @author William-Zack
 */
public class LoginUI extends JFrame {
    // 定义登录界面的组件
    private final JTextField nameField;
    private final JPasswordField passwordField;

    public LoginUI() {
        // 登录UI，进行基础设置
        setTitle("欢迎使用 CourseFlow！");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 创建主面板，使用BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 创建表单面板，使用GridLayout
        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new JLabel("姓名:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        formPanel.add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        // 创建按钮面板，使用FlowLayout
        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("登录");
        buttonPanel.add(loginButton);

        // 将表单面板添加到主面板的中部
        mainPanel.add(formPanel, BorderLayout.CENTER);
        // 将按钮面板添加到主面板的底部
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // 登录按钮事件
        loginButton.addActionListener(e -> {
            String name = nameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticateUser(name, password)) {
                JOptionPane.showMessageDialog(null, "登录成功!");
                // 进入主界面
                new MainUI(name).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(null, "姓名或密码错误!", "登录失败", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private boolean authenticateUser(String name, String password) {
        // 查询数据库，验证用户
        String query = "SELECT * FROM students WHERE name = ? AND password = ? UNION SELECT * FROM teachers WHERE name = ? AND password = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, name);
            stmt.setString(2, password);
            stmt.setString(3, name);
            stmt.setString(4, password);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }
}
