package team.t508.CourseFlow.ui;

import team.t508.CourseFlow.utils.ChatRecord;
import team.t508.CourseFlow.utils.Utility;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.net.Socket;
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

    // 定义网络通信相关的组件
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private Thread receiveThread;

    public MainUI(String userName) {
        // 主界面初始化
        this.userName = userName;
        this.chatRecord = ChatRecord.loadChatRecord(userName);

        // 连接服务器
        connectToServer();

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
                sendMessageToServer(currentTime + " " + userName + ": " + message, currentCourse);
                messageField.setText("");
            }
        });

        // 默认选择第一个课程
        switchCourse("高等数学");
    }

    private void connectToServer() {
        try {
            // 连接服务器
            socket = new Socket("localhost", 8989);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            // 发送用户名到服务器
            dos.writeUTF(userName);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        if (currentCourse != null) {
            // 通知离开当前课程
            sendSystemMessageToServer(userName + " 离开了 " + currentCourse + " 课堂", currentCourse);
        }
        currentCourse = courseName;
        chatArea.setText(courseChatAreas.get(courseName).getText());
        // 通知进入新课程
        sendSystemMessageToServer(userName + " 进入了 " + currentCourse + " 课堂", currentCourse);

        // 重启接收消息的线程以确保只接收当前课程的消息
        if (receiveThread != null && receiveThread.isAlive()) {
            receiveThread.interrupt();
        }
        receiveThread = new Thread(this::receiveMessages);
        receiveThread.start();
    }

    private void sendMessageToServer(String message, String course) {
        try {
            // 先发送课程标签，确保能存储到对应课程中，避免跨聊天收到消息，随后发送具体消息
            dos.writeUTF(course);
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage(message, course);
    }

    private void sendSystemMessageToServer(String message, String course) {
        // 发送系统消息
        String currentTime = Utility.getCurrentDateTime();
        String systemMessage = currentTime + " 系统消息：" + message ;
        try {
            // 先发送课程标签，确保能存储到对应课程中，避免跨聊天收到消息，随后发送具体消息
            dos.writeUTF(course);
            dos.writeUTF(systemMessage);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage(systemMessage, course);
    }

    private void sendMessage(String message, String course) {
        JTextArea currentChatArea = courseChatAreas.get(course);
        currentChatArea.append(message + "\n");
        chatArea.setText(currentChatArea.getText());
        chatRecord.setChatRecord(course, currentChatArea.getText());
        ChatRecord.saveChatRecord(userName, chatRecord);
    }

    private void receiveMessages() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                // 接收发送的课程名和消息
                String course = dis.readUTF();
                String message = dis.readUTF();
                // 如果是当前课程的消息则显示在聊天区域，否则保存到对应课程的聊天记录
                if (course.equals(currentCourse)) {
                    sendMessage(message, course);
                } else {
                    // 保存到其他课程的聊天记录
                    JTextArea otherCourseChatArea = courseChatAreas.get(course);
                    otherCourseChatArea.append(message + "\n");
                    chatRecord.setChatRecord(course, otherCourseChatArea.getText());
                    ChatRecord.saveChatRecord(userName, chatRecord);
                }
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                e.printStackTrace();
            }
        }
    }
}
