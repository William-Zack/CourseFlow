package team.t508.CourseFlow.utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author William-Zack
 */
public class ChatUtils {
    public static void close(Closeable... targets) {
        // 关闭所有Closeable对象
        for (Closeable target : targets) {
            if (target != null) {
                try {
                    target.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
