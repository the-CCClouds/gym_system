package utils;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class DataUtils {

    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT);

    /**
     * 将字符串转换为 java.sql.Date
     * @param dateStr 格式: "yyyy-MM-dd"
     * @return java.sql.Date 对象
     */
    public static Date toSqlDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            java.util.Date utilDate = dateFormat.parse(dateStr);
            return new Date(utilDate.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将 java.sql.Date 转换为字符串
     * @param date java.sql.Date 对象
     * @return 格式: "yyyy-MM-dd"
     */
    public static String toString(Date date) {
        if (date == null) {
            return null;
        }
        return dateFormat.format(date);
    }

    /**
     * 获取当前日期的 java.sql.Date
     * @return 当前日期
     */
    public static Date today() {
        return new Date(System.currentTimeMillis());
    }
}
