package team.t508.CourseFlow.ui;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import team.t508.CourseFlow.utils.DatabaseUtil;

public class LoginUI extends JFrame {
    private final JTextField nameField;
    private final JPasswordField passwordField;

    public LoginUI() {
        setTitle("欢迎使用 CourseFlow！");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        formPanel.add(new JLabel("姓名:"));
        nameField = new JTextField();
        formPanel.add(nameField);
        formPanel.add(new JLabel("密码:"));
        passwordField = new JPasswordField();
        formPanel.add(passwordField);

        JPanel buttonPanel = new JPanel();
        JButton loginButton = new JButton("登录");
        buttonPanel.add(loginButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        loginButton.addActionListener(e -> {
            String userName = nameField.getText();
            String password = String.valueOf(passwordField.getPassword());

            if (validateLogin(userName, password)) {
                dispose();
                new MainUI(userName).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(LoginUI.this, "用户名或密码错误！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private boolean validateLogin(String userName, String password) {
        String role = null;
        try {
            Connection connection = DatabaseUtil.getConnection();
            String sql = "SELECT role FROM users WHERE username=? AND password=?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, userName);
            statement.setString(2, password);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                role = resultSet.getString("role");
            }
            resultSet.close();
            statement.close();
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return role != null && (role.equals("teacher") || role.equals("student"));
    }

    public static void main(String[] args) {
        new LoginUI().setVisible(true);
    }
}
