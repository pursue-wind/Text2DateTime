package cn.mirrorming.text2date.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * @author Mireal
 * @version V1.0
 * @date 2020/3/13 15:24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DateTime {
    private int year;
    private int month;
    private int day;
    private int hour;
    private int min;
    private int sec;

    public Date buildTime() {

        Calendar c = Calendar.getInstance(TimeZone.getTimeZone("GMT+08:00"));
        //获取年
        int year = c.get(Calendar.YEAR);
        //获取月份，0表示1月份
        int month = c.get(Calendar.MONTH) + 1;
        //获取当前天数
        int day = c.get(Calendar.DAY_OF_MONTH);
        //获取当前小时
        int hour = c.get(Calendar.HOUR_OF_DAY);
        //获取当前分钟
        int min = c.get(Calendar.MINUTE);
        //获取当前秒
        int sec = c.get(Calendar.SECOND);

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String timeStr = (this.year == -1 ? year : this.year) + "-"
                + (this.month == -1 ? month : this.month) + "-"
                + (this.day == -1 ? day : this.day) + "-"
                + (this.hour == -1 ? 0 : this.hour) + "-"
                + (this.min == -1 ? 0 : this.min) + "-"
                + (this.sec == -1 ? 0 : this.sec);
        try {
            return sdf.parse(timeStr);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new RuntimeException("时间解析错误");
        }
    }
}
