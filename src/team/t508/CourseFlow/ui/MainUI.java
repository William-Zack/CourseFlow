package team.t508.CourseFlow.ui;

import javax.swing.*;
import java.awt.*;
/**
 * @author henry
 */
public class MainUI extends JFrame {
    public MainUI(String userName) {
        // 主界面基础设置
        setTitle("CourseFlow");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 左侧课程列表面板
        JPanel coursePanel = new JPanel();
        coursePanel.setLayout(new BoxLayout(coursePanel, BoxLayout.Y_AXIS));
        // 添加课程列表项
        coursePanel.add(new JLabel("高等数学"));
        coursePanel.add(new JLabel("计算机组成原理"));
        coursePanel.add(new JLabel("毛泽东思想概论"));

        // 右侧聊天面板
        JPanel chatPanel = new JPanel(new BorderLayout());

        // 上部聊天显示区域
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        // 下部消息输入区域
        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("发送");

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // 将左侧和右侧面板添加到主面板
        mainPanel.add(coursePanel, BorderLayout.WEST);
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        add(mainPanel);

        // 发送按钮事件
        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                chatArea.append(userName + ": " + message + "\n");
                messageField.setText("");
                // 添加发送消息到服务器的逻辑，待写
            }
        });
    }
}
