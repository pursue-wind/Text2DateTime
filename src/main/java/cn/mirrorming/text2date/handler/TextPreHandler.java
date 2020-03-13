package cn.mirrorming.text2date.handler;

import cn.mirrorming.text2date.domain.TextContent;
import cn.mirrorming.text2date.domain.TimeFormat;
import cn.mirrorming.text2date.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本预处理器
 *
 * @author Mireal
 * @version V1.0
 * @date 2020/3/12 20:09
 */
public class TextPreHandler implements TextHandler {
    public final static String[] HOLIDAY = {"小寒", "大寒", "立春", "雨水", "惊蛰", "春分", "清明", "谷雨", "立夏", "小满", "芒种", "夏至", "小暑", "大暑", "立秋", "处暑", "白露", "秋分", "寒露", "霜降", "立冬", "小雪", "大雪", "冬至", "春节", "元宵", "端午", "七夕", "中元", "中秋", "重阳", "腊八", "小年", "除夕", "元旦", "情人节", "妇女节", "植树节", "消费者权益日", "愚人节", "劳动节", "青年节", "护士节", "儿童节", "建党节", "建军节", "爸爸节", "教师节", "孔子诞辰", "国庆节", "老人节", "联合国日", "孙中山诞辰纪念", "澳门回归纪念", "平安夜", "万圣节", "圣诞"};
    private static HashMap<String, String> holidayMap = new HashMap<>();
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日");

    static {
        for (String e : HOLIDAY) {
            holidayMap.put(e, "");
        }
        int year = Calendar.getInstance().get(Calendar.YEAR);
        TimeUtils time = new TimeUtils(year, 1, 1);
        int size = holidayMap.size();
        while (size > 0) {
            // 获取农历节日
            String lh = time.getLunarHoliday();
            // 获取公历节日
            String h = time.getHoliday();
            // 获取节气
            String s = time.getSoralTerm();

            if (holidayMap.containsKey(lh)) {
                String value = sdf.format(time.getCalendar().getTime());
                holidayMap.put(lh, value);
                size--;
            }

            if (holidayMap.containsKey(h)) {
                String value = sdf.format(time.getCalendar().getTime());
                holidayMap.put(h, value);
                size--;
            }

            if (holidayMap.containsKey(s)) {
                String value = sdf.format(time.getCalendar().getTime());
                holidayMap.put(s, value);
                size--;
            }
            time.nextDay();
        }
    }


    @Override
    public Boolean handler(TextContent textContent) {
        String target = textContent.getTarget();
        target = delKeyword(target, "\\s+");
        target = delKeyword(target, "[的]+");
        target = numberTranslator(target);
        target = wordTranslator(target);
        textContent.setTarget(target);
        return true;
    }

    /**
     * 该方法删除一字符串中所有匹配某一规则字串
     * 可用于清理一个字符串中的空白符和语气助词
     *
     * @param target 待处理字符串
     * @param rules  删除规则
     * @return 清理工作完成后的字符串
     */
    public static String delKeyword(String target, String rules) {
        Pattern p = Pattern.compile(rules);
        Matcher m = p.matcher(target);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find();
        while (result) {
            m.appendReplacement(sb, "");
            result = m.find();
        }
        m.appendTail(sb);
        String s = sb.toString();
        return s;
    }

    /**
     * 将字符串中所有的用汉字表示的数字转化为用阿拉伯数字表示的数字
     * 支持了部分不规则表达方法(支持的正确转化范围是0-99999999)
     * <p>
     * 如两万零六百五可转化为20650
     * 两百一十四和两百十四都可以转化为214
     * 一六零加一五八可以转化为160+158
     *
     * @param target 待转化的字符串
     * @return 转化完毕后的字符串
     */
    public static String numberTranslator(String target) {
        Pattern p = Pattern.compile("[一二两三四五六七八九123456789]万[一二两三四五六七八九123456789](?!(千|百|十))");
        Matcher m = p.matcher(target);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find();
        while (result) {
            String group = m.group();
            String[] s = group.split("万");
            int num = 0;
            if (s.length == 2) {
                num += word2Number(s[0]) * 10000 + word2Number(s[1]) * 1000;
            }
            m.appendReplacement(sb, Integer.toString(num));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();
        p = Pattern.compile("[一二两三四五六七八九123456789]千[一二两三四五六七八九123456789](?!(百|十))");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String group = m.group();
            String[] s = group.split("千");
            int num = 0;
            if (s.length == 2) {
                num += word2Number(s[0]) * 1000 + word2Number(s[1]) * 100;
            }
            m.appendReplacement(sb, Integer.toString(num));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        p = Pattern.compile("[一二两三四五六七八九123456789]百[一二两三四五六七八九123456789](?!十)");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String group = m.group();
            String[] s = group.split("百");
            int num = 0;
            if (s.length == 2) {
                num += word2Number(s[0]) * 100 + word2Number(s[1]) * 10;
            }
            m.appendReplacement(sb, Integer.toString(num));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        p = Pattern.compile("[零一二两三四五六七八九]");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, Integer.toString(word2Number(m.group())));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        p = Pattern.compile("(?<=(周|星期|礼拜))[末天日]");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, Integer.toString(word2Number(m.group())));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        p = Pattern.compile("(?<!(周|星期))0?[0-9]?十[0-9]?");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String group = m.group();
            String[] s = group.split("十");
            int num = 0;
            if (s.length == 0) {
                num += 10;
            } else if (s.length == 1) {
                int ten = Integer.parseInt(s[0]);
                if (ten == 0) {
                    num += 10;
                } else {
                    num += ten * 10;
                }
            } else if (s.length == 2) {
                if (s[0].equals("")) {
                    num += 10;
                } else {
                    int ten = Integer.parseInt(s[0]);
                    if (ten == 0) {
                        num += 10;
                    } else {
                        num += ten * 10;
                    }
                }
                num += Integer.parseInt(s[1]);
            }
            m.appendReplacement(sb, Integer.toString(num));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        p = Pattern.compile("0?[1-9]百[0-9]?[0-9]?");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String group = m.group();
            String[] s = group.split("百");
            int num = 0;
            if (s.length == 1) {
                int hundred = Integer.parseInt(s[0]);
                num += hundred * 100;
            } else if (s.length == 2) {
                int hundred = Integer.parseInt(s[0]);
                num += hundred * 100;
                num += Integer.parseInt(s[1]);
            }
            m.appendReplacement(sb, Integer.toString(num));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        p = Pattern.compile("0?[1-9]千[0-9]?[0-9]?[0-9]?");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String group = m.group();
            String[] s = group.split("千");
            int num = 0;
            if (s.length == 1) {
                int thousand = Integer.parseInt(s[0]);
                num += thousand * 1000;
            } else if (s.length == 2) {
                int thousand = Integer.parseInt(s[0]);
                num += thousand * 1000;
                num += Integer.parseInt(s[1]);
            }
            m.appendReplacement(sb, Integer.toString(num));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        p = Pattern.compile("[0-9]+万[0-9]?[0-9]?[0-9]?[0-9]?");
        m = p.matcher(target);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String group = m.group();
            String[] s = group.split("万");
            int num = 0;
            if (s.length == 1) {
                int tenthousand = Integer.parseInt(s[0]);
                num += tenthousand * 10000;
            } else if (s.length == 2) {
                int tenthousand = Integer.parseInt(s[0]);
                num += tenthousand * 10000;
                num += Integer.parseInt(s[1]);
            }
            m.appendReplacement(sb, Integer.toString(num));
            result = m.find();
        }
        m.appendTail(sb);
        target = sb.toString();

        return target;
    }

    /**
     * 方法numberTranslator的辅助方法，将[零-九]翻译为[0-9]
     *
     * @param s 大写数字
     * @return 对应的整形数，如果不是大写数字返回-1
     */
    private static int word2Number(String s) {
        if ("零".equals(s) || "0".equals(s)) {
            return 0;
        } else if ("一".equals(s) || "1".equals(s)) {
            return 1;
        } else if ("二".equals(s) || "两".equals(s) || "2".equals(s)) {
            return 2;
        } else if ("三".equals(s) || "3".equals(s)) {
            return 3;
        } else if ("四".equals(s) || "4".equals(s)) {
            return 4;
        } else if ("五".equals(s) || "5".equals(s)) {
            return 5;
        } else if ("六".equals(s) || "6".equals(s)) {
            return 6;
        } else if ("七".equals(s) || "天".equals(s) || "日".equals(s) || "末".equals(s) || "7".equals(s)) {
            return 7;
        } else if ("八".equals(s) || "8".equals(s)) {
            return 8;
        } else if ("九".equals(s) || "9".equals(s)) {
            return 9;
        } else {
            return -1;
        }
    }

    public static String wordTranslator(String target) {
        String rules = "今明天|今天明天|今明天|今明两天|今明2天";
        Pattern p = Pattern.compile(rules);
        Matcher m = p.matcher(target);
        StringBuffer sb = new StringBuffer();
        boolean result = m.find();
        while (result) {
            m.appendReplacement(sb, "今天到明天");
            result = m.find();
        }
        m.appendTail(sb);
        String s = sb.toString();

        rules = "今儿";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, "今天");
            result = m.find();
        }
        m.appendTail(sb);
        s = sb.toString();


        rules = "明后天|明天后天|未来2天|最近2天|未来两天|最近两天|明后两天|明后2天|接下来两天";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, "明天到后天");
            result = m.find();
        }
        m.appendTail(sb);
        s = sb.toString();


        rules = "最近几天|未来几天|随后几天|未来3天|最近3天|这3天|未来三天|最近三天|这三天|这几天|这些天|往后三天|往后3天|接下来三天";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, "明天到大后天");
            result = m.find();
        }
        m.appendTail(sb);
        s = sb.toString();

        rules = "礼拜";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, "星期");
            result = m.find();
        }
        m.appendTail(sb);
        s = sb.toString();

        rules = "(这周|这星期|这一周|这一星期|最近一周|这1周|这1星期|最近1周)";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, "周1到周7");
            result = m.find();
        }
        m.appendTail(sb);
        s = sb.toString();
        rules = "[一二两三四五六七八九十0123456789]{1,2}[号日][一二两三四五六七八九十0123456789]{1,2}[号日]";
        p = Pattern.compile(rules);
        m = p.matcher(s);

        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String rules2 = "号|日";
            Pattern p2 = p = Pattern.compile(rules2);
            Matcher m2 = p.matcher(m.group(0));
            StringBuffer sb2 = new StringBuffer();
            boolean result2 = m2.find();
            while (result2) {
                m2.appendReplacement(sb2, "号和");
                result2 = m2.find();
            }
            m2.appendTail(sb2);
            String s2 = sb2.toString();
            m.appendReplacement(sb, s2);
            result = m.find();
        }

        m.appendTail(sb);
        s = sb.toString();

        rules = "(周|星期)[1234567日天](周|星期)[1234567日天]";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            String rules2 = "周|星期";
            Pattern p2 = p = Pattern.compile(rules2);
            Matcher m2 = p.matcher(m.group(0));
            StringBuffer sb2 = new StringBuffer();
            boolean result2 = m2.find();
            while (result2) {
                m2.appendReplacement(sb2, "和周");
                result2 = m2.find();
            }
            m2.appendTail(sb2);
            String s2 = sb2.toString();
            m.appendReplacement(sb, s2);
            result = m.find();
        }

        m.appendTail(sb);
        s = sb.toString();
        rules = "圣诞节|平安夜|父亲节|元旦|除夕|春节|清明节|劳动节|端午节|中秋节|国庆节|母亲节|儿童节|建军节|愚人节|青年节圣诞节|平安夜|"
                + "教师节|万圣节|植树节|重阳节|腊八节|情人节|元宵节|感恩节|妇女节|小年|五一|六一|七一|八一|九一|十一";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();

        while (result) {
            TimeFormat tfFormat = normTime(m.group());
            m.appendReplacement(sb, tfFormat.getStartTime());
            result = m.find();
        }
        m.appendTail(sb);
        s = sb.toString();
        rules = "最近";
        p = Pattern.compile(rules);
        m = p.matcher(s);
        sb = new StringBuffer();
        result = m.find();
        while (result) {
            m.appendReplacement(sb, "明天和后天");
            result = m.find();
        }
        m.appendTail(sb);
        s = sb.toString();
        return s;
    }


    // 主要函数
    public static TimeFormat normTime(String text) {
        TimeFormat tf = null;
        try {
            tf = holidayDate(text);
            if (tf == null) {
                tf = weekTime(text);
            }
            if (tf == null) {
                tf = day(text);
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return tf;
    }


    private static TimeFormat day(String text) {
        TimeFormat tf = null;
        text = text.trim();
        String[] h = {"五一", "六一", "七一", "八一", "九一", "十一"};
        for (int i = 0; i < h.length; i++) {
            String e = h[i];
            if (text.equals(e)) {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.MONTH, i + 4);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                String moveTime = sdf.format(cal.getTime());
                tf = new TimeFormat(moveTime, moveTime, null);
            }
        }
        return tf;
    }


    // 节日转化为时间
    private static TimeFormat holidayDate(String holiday) {
        String date = null;
        if (holidayMap.containsKey(holiday)) {
            date = holidayMap.get(holiday);
        } else if (!holidayMap.containsKey(holiday) && holiday.contains("节")) {
            holiday = holiday.replace("节", "");
            date = holidayMap.get(holiday);
        }

        TimeFormat tf = null;
        if (date != null) {
            tf = new TimeFormat(date, date, null);
        }

        return tf;
    }


    private static TimeFormat weekTime(String text) {
        TimeFormat tf = null;
        // 处理 感恩节、父亲节、母亲节等这类的节日
        if (text.trim().contains("节")) {
            String[] s = {"感恩节", "父亲节", "母亲节"};
            for (int j = 0; j < s.length; j++) {
                Calendar cal = Calendar.getInstance();
                if (text.equals(s[0])) {
                    cal.set(Calendar.MONTH, 10);
                    cal.set(Calendar.WEEK_OF_MONTH, 4);
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                } else if (text.equals(s[1])) {
                    cal.set(Calendar.MONTH, 5);
                    cal.set(Calendar.WEEK_OF_MONTH, 3);
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                } else if (text.equals(s[2])) {
                    cal.set(Calendar.MONTH, 4);
                    cal.set(Calendar.WEEK_OF_MONTH, 2);
                    cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                }
                String moveTime = sdf.format(cal.getTime());
                tf = new TimeFormat(moveTime, moveTime, null);

            }
        }
        return tf;
    }
}
