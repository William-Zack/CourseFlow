package team.t508.CourseFlow.server;

import team.t508.CourseFlow.utils.ChatUtils;
import team.t508.CourseFlow.utils.Utility;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author William-Zack
 * @author GavenZ
 */
public class ServerMultiUser {
    // 定义一个ArrayList，用于存储所有客户端的Channel对象
    private static CopyOnWriteArrayList<Channel> allClient = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        // 创建服务器
        System.out.println("----服务器启动----");
        ServerSocket server = new ServerSocket(8989);

        // 服务器循环监听客户端连接
        while (true) {
            Socket client = server.accept();
            Channel channel = new Channel(client);
            allClient.add(channel);
            new Thread(channel).start();
        }
    }

    static class Channel implements Runnable {
        // 定义DataInputStream和DataOutputStream对象，用于接收和发送数据
        private java.io.DataInputStream dis;
        private java.io.DataOutputStream dos;
        // 定义Socket对象，用于接收客户端的连接
        private Socket client;
        // isRunning用于判断是否继续接收数据
        private boolean isRunning;
        // name用于存储客户端的用户名，用于区分不同的客户端，可在后续数据库存储中使用
        private String name;
        // currentCourse用于存储当前课程，用于区分不同课程的聊天
        private String currentCourse;

        public Channel(Socket client) {
            // 初始化Channel对象，接收客户端的Socket对象
            this.client = client;
            try {
                dis = new java.io.DataInputStream(client.getInputStream());
                dos = new java.io.DataOutputStream(client.getOutputStream());
                this.isRunning = true;
                this.name = receive();
            } catch (IOException e) {
                release();
            }
        }

        private String receive() {
            // 接收数据，先定义一个空字符串，用于存储接收到的数据
            String msg = "";
            try {
                // 读取UTF格式的字符串，如果读取失败，则调用release()方法关闭连接
                msg = dis.readUTF();
            } catch (IOException e) {
                release();
            }
            // 返回接收到的数据
            return msg;
        }

        private void send(String course, String msg) {
            try {
                // 发送数据，先发送课程标签，确保能存储到对应课程中，避免跨聊天收到消息，随后发送具体消息
                dos.writeUTF(course);
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                // 发送失败时调用release()方法关闭连接
                release();
            }
        }

        private void sendOthers(String course, String msg, boolean isSys) {
            // 遍历所有客户端，将消息发送给除自己以外的所有客户端
            for (Channel other : allClient) {
                if (other == this) {
                    continue;
                }
                other.send(course, msg);
            }
        }

        private void release() {
            // 通知其他客户端用户离开
            if (currentCourse != null) {
                sendOthers(currentCourse, Utility.getCurrentDateTime() + " 系统消息：" + name + " 离开了 " + currentCourse + " 课堂", true);
            }
            // 关闭连接，释放资源
            this.isRunning = false;
            ChatUtils.close(dis, dos, client);
            allClient.remove(this);
        }

        @Override
        public void run() {
            // 覆写run()方法，用于接收客户端发送的数据
            while (isRunning) {
                currentCourse = receive();
                String msg = receive();
                if (!msg.isEmpty()) {
                    if ("CHECK_IN_REQUEST".equals(msg)) {
                        handleCheckInRequest();
                    } else if (msg.startsWith("CHECK_IN_RESPONSE:")) {
                        String studentName = msg.split(":")[1];
                        sendOthers(currentCourse, Utility.getCurrentDateTime() + " 系统消息：" + studentName + " 完成签到", true);
                    } else {
                        sendOthers(currentCourse, msg, false);
                    }
                }
            }
        }

        private void handleCheckInRequest() {
            sendOthers(currentCourse, "CHECK_IN_REQUEST", true);
        }
    }
}
