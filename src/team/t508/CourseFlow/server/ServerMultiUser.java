package team.t508.CourseFlow.server;

import team.t508.CourseFlow.utils.ChatUtils;
import team.t508.CourseFlow.utils.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author William-Zack
 * @author Gavenz
 */

public class ServerMultiUser {
    // 存储所有连接的客户端通道
    private static CopyOnWriteArrayList<Channel> allClient = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("----服务器启动----");
        // 创建服务器端Socket，监听端口8989
        java.net.ServerSocket server = new java.net.ServerSocket(8989);

        while (true) {
            // 循环等待客户端连接
            Socket client = server.accept();
            // 为每个客户端创建一个通道
            Channel channel = new Channel(client);
            // 将新通道添加到客户端列表
            allClient.add(channel);
            // 启动通道线程
            new Thread(channel).start();
        }
    }

    static class Channel implements Runnable {
        private DataInputStream dis;
        private DataOutputStream dos;
        private Socket client;
        private boolean isRunning;
        private String name;
        private String currentCourse;

        public Channel(Socket client) {
            this.client = client;
            try {
                // 初始化数据流
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());
                this.isRunning = true;
                // 接收客户端发来的用户名
                this.name = receive();
            } catch (IOException e) {
                // 处理初始化异常
                release();
            }
        }

        // 接收消息的方法
        private String receive() {
            String msg = "";
            try {
                msg = dis.readUTF();
            } catch (IOException e) {
                release();
            }
            return msg;
        }

        // 发送消息的方法
        private void send(String course, String msg) {
            try {
                dos.writeUTF(course);
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                release();
            }
        }

        // 发送消息给其他用户的方法
        private void sendOthers(String course, String msg, boolean isSys) {
            for (Channel other : allClient) {
                if (other == this) {
                    continue;
                }
                other.send(course, msg);
            }
        }

        // 释放资源的方法
        private void release() {
            if (currentCourse != null) {
                sendOthers(currentCourse, Utility.getCurrentDateTime() + " 系统消息：" + name + " 离开了 " + currentCourse + " 课堂", true);
            }
            this.isRunning = false;
            // 关闭数据流和Socket
            ChatUtils.close(dis, dos, client);
            // 从客户端列表中移除当前通道
            allClient.remove(this);
        }

        @Override
        public void run() {
            while (isRunning) {
                // 循环监听消息
                currentCourse = receive();
                String msg = receive();

                if (!msg.isEmpty()) {
                    if (msg.contains("签到成功")) {
                        sendOthers(currentCourse, msg, false);
                    } else {
                        sendOthers(currentCourse, msg, false);
                    }
                }
            }
        }
    }
}
