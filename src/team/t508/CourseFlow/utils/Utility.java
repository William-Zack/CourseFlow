package team.t508.CourseFlow.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author William-Zack
 */
public class Utility {
    // 获取当前日期和时间的字符串表示
    public static String getCurrentDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
