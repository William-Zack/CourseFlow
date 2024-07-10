package team.t508.CourseFlow.utils;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
/**
 * @author Henry
 */
public class ChatRecord implements Serializable {
    // 课程聊天记录
    private static final long serialVersionUID = 1L;
    private final Map<String, String> courseChatRecords;

    public ChatRecord() {
        // 初始化课程聊天记录
        courseChatRecords = new HashMap<>();
    }

    public void setChatRecord(String course, String record) {
        // 设置课程聊天记录
        courseChatRecords.put(course, record);
    }

    public String getChatRecord(String course) {
        // 获取课程聊天记录
        return courseChatRecords.getOrDefault(course, "");
    }

    public static ChatRecord loadChatRecord(String userName) {
        // 加载聊天记录
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(userName + "_chatRecord.ser"))) {
            // 读取聊天记录
            return (ChatRecord) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            // 读取失败
            return new ChatRecord();
        }
    }

    public static void saveChatRecord(String userName, ChatRecord chatRecord) {
        // 保存聊天记录
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(userName + "_chatRecord.ser"))) {
            // 保存聊天记录
            oos.writeObject(chatRecord);
        } catch (IOException e) {
            // 保存失败
            e.printStackTrace();
        }
    }
}
