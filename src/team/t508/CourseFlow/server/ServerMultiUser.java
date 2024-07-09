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

public class ServerMultiUser {    // 多用户服务器
    private static CopyOnWriteArrayList<Channel> allClient = new CopyOnWriteArrayList<>();    // 线程安全的ArrayList

    public static void main(String[] args) throws IOException {
        System.out.println("----服务器启动----");
        java.net.ServerSocket server = new java.net.ServerSocket(8989);

        while (true) {    // 循环监听
            Socket client = server.accept();
            Channel channel = new Channel(client);
            allClient.add(channel);
            new Thread(channel).start();
        }
    }

    static class Channel implements Runnable {    // 通道
        private java.io.DataInputStream dis;
        private java.io.DataOutputStream dos;
        private Socket client;
        private boolean isRunning;
        private String name;
        private String currentCourse;

        public Channel(Socket client) {    // 构造方法
            this.client = client;
            try {    // 初始化
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());
                this.isRunning = true;
                this.name = receive();
            } catch (IOException e) {    // 异常处理
                release();
            }
        }

        private String receive() {    // 接收消息
            String msg = "";
            try {
                msg = dis.readUTF();
            } catch (IOException e) {    // 异常处理
                release();
            }
            return msg;    // 返回消息
        }

        private void send(String course, String msg) {    // 发送消息
            try {
                dos.writeUTF(course);
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                release();
            }
        }

        private void sendOthers(String course, String msg, boolean isSys) {    // 发送消息给其他用户
            for (Channel other : allClient) {
                if (other == this) {
                    continue;
                }
                other.send(course, msg);
            }
        }

        private void release() {    // 释放资源
            if (currentCourse != null) {
                sendOthers(currentCourse, Utility.getCurrentDateTime() + " 系统消息：" + name + " 离开了 " + currentCourse + " 课堂", true);
            }
            this.isRunning = false;
            ChatUtils.close(dis, dos, client);
            allClient.remove(this);
        }

        @Override
        public void run() {    // 运行
            while (isRunning) {    // 循环监听
                currentCourse = receive();
                String msg = receive();

                if (!msg.isEmpty()) {    // 判断消息是否为空
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
