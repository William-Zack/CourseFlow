package team.t508.CourseFlow.ui;

import team.t508.CourseFlow.utils.ChatRecord;
import team.t508.CourseFlow.utils.Utility;

import javax.swing.*;
import java.awt.*;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class MainUI extends JFrame {
    private final JTextArea chatArea;
    private final Map<String, JTextArea> courseChatAreas;
    private String currentCourse;
    private final ChatRecord chatRecord;
    private final String userName;

    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    public MainUI(String userName) {
        this.userName = userName;
        this.chatRecord = ChatRecord.loadChatRecord(userName);

        connectToServer();

        setTitle("CourseFlow");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout());
        JPanel coursePanel = new JPanel();
        coursePanel.setLayout(new BoxLayout(coursePanel, BoxLayout.Y_AXIS));
        courseChatAreas = new HashMap<>();

        addCourse("高等数学", coursePanel);
        addCourse("计算机组成原理", coursePanel);
        addCourse("毛泽东思想概论", coursePanel);

        JPanel chatPanel = new JPanel(new BorderLayout());

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        JPanel inputPanel = new JPanel(new BorderLayout());
        JTextField messageField = new JTextField();
        JButton sendButton = new JButton("发送");

        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        mainPanel.add(coursePanel, BorderLayout.WEST);
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        add(mainPanel);

        sendButton.addActionListener(e -> {
            String message = messageField.getText();
            if (!message.isEmpty()) {
                String currentTime = Utility.getCurrentDateTime();
                sendMessageToServer(currentTime + " " + userName + ": " + message, currentCourse);
                messageField.setText("");
            }
        });

        switchCourse("高等数学");

        new Thread(this::receiveMessages).start();

        // 根据用户名区分老师和学生界面
        if (userName.equals("teacher1")) {
            // 老师界面的按钮：签到
            JButton attendanceButton = new JButton("签到");
            attendanceButton.addActionListener(e -> {
                sendSystemMessageToServer(userName + " 发起了签到", currentCourse);
            });
            inputPanel.add(attendanceButton, BorderLayout.WEST);
        } else if (userName.equals("student1")) {
            // 学生界面
        }
    }

    private void connectToServer() {
        try {
            socket = new Socket("localhost", 8989);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
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
            sendSystemMessageToServer(userName + " 离开了 " + currentCourse + " 课堂", currentCourse);
        }
        currentCourse = courseName;
        chatArea.setText(courseChatAreas.get(courseName).getText());
        sendSystemMessageToServer(userName + " 进入了 " + currentCourse + " 课堂", currentCourse);
    }

    private void sendMessageToServer(String message, String course) {
        try {
            dos.writeUTF(course);
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage(message, course);
    }

    private void sendSystemMessageToServer(String message, String course) {
        String currentTime = Utility.getCurrentDateTime();
        String systemMessage = currentTime + " 系统消息：" + message;
        try {
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
            while (true) {
                String course = dis.readUTF();
                String message = dis.readUTF();
                if (course.equals(currentCourse)) {
                    sendMessage(message, course);
                } else {
                    JTextArea otherCourseChatArea = courseChatAreas.get(course);
                    otherCourseChatArea.append(message + "\n");
                    chatRecord.setChatRecord(course, otherCourseChatArea.getText());
                    ChatRecord.saveChatRecord(userName, chatRecord);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
