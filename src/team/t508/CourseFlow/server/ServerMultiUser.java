package team.t508.CourseFlow.server;

import team.t508.CourseFlow.utils.ChatUtils;
import team.t508.CourseFlow.utils.Utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.CopyOnWriteArrayList;

public class ServerMultiUser {
    private static CopyOnWriteArrayList<Channel> allClient = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        System.out.println("----服务器启动----");
        java.net.ServerSocket server = new java.net.ServerSocket(8989);

        while (true) {
            Socket client = server.accept();
            Channel channel = new Channel(client);
            allClient.add(channel);
            new Thread(channel).start();
        }
    }

    static class Channel implements Runnable {
        private java.io.DataInputStream dis;
        private java.io.DataOutputStream dos;
        private Socket client;
        private boolean isRunning;
        private String name;
        private String currentCourse;

        public Channel(Socket client) {
            this.client = client;
            try {
                dis = new DataInputStream(client.getInputStream());
                dos = new DataOutputStream(client.getOutputStream());
                this.isRunning = true;
                this.name = receive();
            } catch (IOException e) {
                release();
            }
        }

        private String receive() {
            String msg = "";
            try {
                msg = dis.readUTF();
            } catch (IOException e) {
                release();
            }
            return msg;
        }

        private void send(String course, String msg) {
            try {
                dos.writeUTF(course);
                dos.writeUTF(msg);
                dos.flush();
            } catch (IOException e) {
                release();
            }
        }

        private void sendOthers(String course, String msg, boolean isSys) {
            for (Channel other : allClient) {
                if (other == this) {
                    continue;
                }
                other.send(course, msg);
            }
        }

        private void release() {
            if (currentCourse != null) {
                sendOthers(currentCourse, Utility.getCurrentDateTime() + " 系统消息：" + name + " 离开了 " + currentCourse + " 课堂", true);
            }
            this.isRunning = false;
            ChatUtils.close(dis, dos, client);
            allClient.remove(this);
        }

        @Override
        public void run() {
            while (isRunning) {
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
