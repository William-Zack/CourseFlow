package team.t508.CourseFlow.ui;

import team.t508.CourseFlow.utils.ChatRecord;
import team.t508.CourseFlow.utils.Utility;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Henry
 */
public class MainUI extends JFrame {
    // 定义主界面的组件
    private final JTextArea chatArea;
    private final Map<String, JTextArea> courseChatAreas;
    private String currentCourse;
    private final ChatRecord chatRecord;
    private final String userName;

    public MainUI(String userName) {
        // 主界面初始化
        this.userName = userName;
        this.chatRecord = ChatRecord.loadChatRecord(userName);

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
        courseChatAreas = new HashMap<>();

        // 添加课程列表项
        addCourse("高等数学", coursePanel);
        addCourse("计算机组成原理", coursePanel);
        addCourse("毛泽东思想概论", coursePanel);

        // 右侧聊天面板
        JPanel chatPanel = new JPanel(new BorderLayout());

        // 上部聊天显示区域
        chatArea = new JTextArea();
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
                String currentTime = Utility.getCurrentDateTime();
                sendMessage(currentTime + " " + userName + ": " + message);
                messageField.setText("");
                // 添加发送消息到服务器的逻辑，待写
            }
        });

        // 默认选择第一个课程
        switchCourse("高等数学");
    }

    private void addCourse(String courseName, JPanel coursePanel) {
        JButton courseButton = new JButton(courseName);
        courseButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, courseButton.getMinimumSize().height));
        courseButton.addActionListener(e -> switchCourse(courseName));
        coursePanel.add(courseButton);
        JTextArea courseChatArea = new JTextArea();
        courseChatArea.setText(chatRecord.getChatRecord(courseName));
        courseChatAreas.put(courseName, courseChatArea);
    }

    private void switchCourse(String courseName) {
        currentCourse = courseName;
        chatArea.setText(courseChatAreas.get(courseName).getText());
    }

    private void sendMessage(String message) {
        JTextArea currentChatArea = courseChatAreas.get(currentCourse);
        currentChatArea.append(message + "\n");
        chatArea.setText(currentChatArea.getText());
        chatRecord.setChatRecord(currentCourse, currentChatArea.getText());
        ChatRecord.saveChatRecord(userName, chatRecord);
        // 添加发送消息到服务器的逻辑，待写
    }
}
