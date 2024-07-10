package team.t508.CourseFlow.ui;

import team.t508.CourseFlow.utils.ChatRecord;
import team.t508.CourseFlow.utils.ChatUtils;
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
 * @author Gavenz
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

    public MainUI(String userName, boolean isTeacher) {
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

        // 添加窗口监听器
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                sendSystemMessageToServer(userName + " 离开了 " + currentCourse + " 课堂", currentCourse);
                ChatUtils.close(dis, dos, socket); // 关闭资源
                System.exit(0);
            }
        });

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
        // 将消息输入框和发送按钮添加到输入面板
        inputPanel.add(messageField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        // 将聊天显示区域和输入面板添加到聊天面板
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);
        chatPanel.add(inputPanel, BorderLayout.SOUTH);

        // 添加签到和举手按钮
        JPanel buttonPanel = new JPanel();
        if (isTeacher) {   // 如果是老师，显示签到按钮
            JButton checkInButton = new JButton("签到");
            checkInButton.addActionListener(e -> sendCheckInRequest());
            buttonPanel.add(checkInButton);
        } else {    // 如果是学生，显示举手按钮
            JButton raiseHandButton = new JButton("举手");
            raiseHandButton.addActionListener(e -> sendRaiseHandRequest());
            buttonPanel.add(raiseHandButton);
        }

        // 将按钮面板添加到聊天面板的上方
        chatPanel.add(buttonPanel, BorderLayout.NORTH);

        // 将左侧和右侧面板添加到主面板
        mainPanel.add(coursePanel, BorderLayout.WEST);
        mainPanel.add(chatPanel, BorderLayout.CENTER);

        add(mainPanel); // 将主面板添加到窗口

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

        // 启动线程接收消息
        new Thread(this::receiveMessages).start();
    }

    private void connectToServer() {
        try {
            // 连接服务器
            socket = new Socket("localhost", 8989);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            // 发送用户名到服务器
            dos.writeUTF(userName);
        } catch (IOException e) { // 连接失败
            e.printStackTrace();
        }
    }

    private void addCourse(String courseName, JPanel coursePanel) { // 添加课程
        JButton courseButton = new JButton(courseName);    // 创建课程按钮
        courseButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, courseButton.getMinimumSize().height));    // 设置按钮宽度自适应
        courseButton.addActionListener(e -> switchCourse(courseName));      // 为课程按钮添加事件监听器
        coursePanel.add(courseButton);      // 将课程按钮添加到课程面板
        JTextArea courseChatArea = new JTextArea();       // 创建课程聊天区域
        courseChatArea.setText(chatRecord.getChatRecord(courseName));    // 设置课程聊天区域的文本
        courseChatAreas.put(courseName, courseChatArea);    // 将课程聊天区域添加到课程聊天区域Map
    }

    private void switchCourse(String courseName) {    // 切换课程
        if (currentCourse != null) {
            // 通知离开当前课程
            sendSystemMessageToServer(userName + " 离开了 " + currentCourse + " 课堂", currentCourse);
        }
        currentCourse = courseName;
        chatArea.setText(courseChatAreas.get(courseName).getText());
        // 通知进入新课程
        sendSystemMessageToServer(userName + " 进入了 " + currentCourse + " 课堂", currentCourse);
    }

    private void sendMessageToServer(String message, String course) {     // 发送消息到服务器
        try {
            // 先发送课程标签，确保能存储到对应课程中，避免跨聊天收到消息，随后发送具体消息
            dos.writeUTF(course);
            dos.writeUTF(message);
            dos.flush();
        } catch (IOException e) {     // 发送失败
            e.printStackTrace();
        }
        sendMessage(message, course);
    }

    private void sendSystemMessageToServer(String message, String course) {    // 发送系统消息到服务器
        String currentTime = Utility.getCurrentDateTime();
        String systemMessage = currentTime + " 系统消息：" + message ;
        try {
            // 先发送课程标签，确保能存储到对应课程中，避免跨聊天收到消息，随后发送具体消息
            dos.writeUTF(course);
            dos.writeUTF(systemMessage);
            dos.flush();
        } catch (IOException e) {    // 发送失败
            e.printStackTrace();
        }
        sendMessage(systemMessage, course);
    }

    private void sendMessage(String message, String course) {    // 发送消息
        JTextArea currentChatArea = courseChatAreas.get(course);    // 获取当前课程的聊天区域
        currentChatArea.append(message + "\n");    // 在聊天区域中添加消息
        chatArea.setText(currentChatArea.getText());    // 更新主聊天区域
        chatRecord.setChatRecord(course, currentChatArea.getText());    // 更新聊天记录
        ChatRecord.saveChatRecord(userName, chatRecord);    // 保存聊天记录
    }

    private void receiveMessages() {    // 接收消息
        try {    // 循环接收消息
            while (true) {
                // 接收发送的课程名和消息
                String course = dis.readUTF();
                String message = dis.readUTF();
                // 如果是签到请求，则处理签到请求
                if (course.equals(currentCourse)) {    // 如果是当前课程的消息
                    if (message.equals("CHECK_IN_REQUEST")) {
                        handleCheckInRequest();
                    } else {
                        sendMessage(message, course);
                    }
                } else {    // 如果不是当前课程的消息，则保存到其他课程的聊天记录
                    if (!message.equals("CHECK_IN_REQUEST")){
                        // 保存到其他课程的聊天记录，if判断避免保存签到请求
                        JTextArea otherCourseChatArea = courseChatAreas.get(course);
                        otherCourseChatArea.append(message + "\n");
                        chatRecord.setChatRecord(course, otherCourseChatArea.getText());
                        ChatRecord.saveChatRecord(userName, chatRecord);
                    }
                }
            }
        } catch (IOException e) {    // 接收失败
            e.printStackTrace();
        }
    }


    private void sendCheckInRequest() {    // 发送签到请求
        try {
            dos.writeUTF(currentCourse); // 发送课程标签
            dos.writeUTF("CHECK_IN_REQUEST"); // 发送签到请求
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendRaiseHandRequest() {    // 发送举手请求
        String raiseHandMessage = Utility.getCurrentDateTime() + " 系统消息：" + userName + " 举手了";
        try {
            // 先发送课程标签，随后发送具体消息
            dos.writeUTF(currentCourse);
            dos.writeUTF(raiseHandMessage);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        sendMessage(raiseHandMessage, currentCourse);
    }

    private void handleCheckInRequest() {    // 处理签到请求
        int result = JOptionPane.showConfirmDialog(this, "老师发起了签到请求，是否签到？", "签到请求", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            try {
                dos.writeUTF(currentCourse); // 发送课程标签
                dos.writeUTF("CHECK_IN_RESPONSE:" + userName); // 发送签到响应
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
