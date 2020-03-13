package cn.mirrorming.text2date.handler;

import cn.mirrorming.text2date.domain.DateTime;
import cn.mirrorming.text2date.domain.TextContent;
import cn.mirrorming.text2date.domain.RangeTimeEnum;
import lombok.Data;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 时间上下文处理器
 *
 * @author Mireal Chen
 * @version V1.0
 * @date 2020/3/12 21:17
 */
@Data
public class TimeContextHandler implements TextHandler {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    @Override
    public Boolean handler(TextContent textContent) {
        List<String> tempList = textContent.getTempList();
        String baseTime = textContent.getCurTime();
        String target = textContent.getTarget();

        List<Date> collect = tempList.stream().map(time -> {
            int year = normSetyear(time);
            int month = normSetmonth(time);
            int day = normSetday(time);
            int hour = normSethour(time);
            int min = normSetminute(time);
            int sec = normSetsecond(time);
            DateTime dateTime = new DateTime(year, month, day, hour, min, sec);
            //如果年月日时分秒都是-1 ，判断是不是偏移量 //如果是偏移量，则传入target判断是前还是后
//            if (year == -1 && month == -1 && day == -1 && hour == -1 && min == -1 && sec == -1) {      }
            dateTime = timeOffsetCalc(baseTime, time, dateTime);
            dateTime = normSetCurRelated(baseTime, time, dateTime);
            dateTime = normSetTotal(time, dateTime);
//            Calendar calendar = buildCalendar(dateTime);
            Date date = dateTime.buildTime();
            return date;

        }).collect(Collectors.toList());

//        normSetmonthFuzzyday(time);

        textContent.setResultTime(collect);
        collect.forEach(c -> {
            String format = sdf.format(c);
            System.err.println("------------------>" + format);
        });
        System.err.println("========================================================");
        return true;
    }


    /**
     * 年-规范化方法
     * <p>
     * 识别时间表达式单元的年字段
     */
    public int normSetyear(String timeExpression) {
        int resultYear = -1;
        /**假如只有两位数来表示年份*/
        String rule = "[0-9]{2}(?=年)";
        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            resultYear = Integer.parseInt(match.group());
            if (resultYear >= 0 && resultYear < 100) {
                if (resultYear < 30) {
                    /**30以下表示2000年以后的年份*/
                    resultYear += 2000;
                } else {
                    /**否则表示1900年以后的年份*/
                    resultYear += 1900;
                }
            }

        }
        /**不仅局限于支持1XXX年和2XXX年的识别，可识别三位数和四位数表示的年份*/
        rule = "[0-9]?[0-9]{3}(?=年)";

        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find())/**如果有3位数和4位数的年份，则覆盖原来2位数识别出的年份*/ {
            resultYear = Integer.parseInt(match.group());
        }
        return resultYear;
    }

    /**
     * 月-规范化方法
     * <p>
     * 识别时间表达式单元的月字段
     */
    public int normSetmonth(String timeExpression) {
        int resultMonth = -1;
        String rule = "((10)|(11)|(12)|([1-9]))(?=月)";
        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            resultMonth = Integer.parseInt(match.group());
            /**处理倾向于未来时间的情况 */
            //preferFuture(1);
        }
        return resultMonth;
    }

    /**
     * 月-日 兼容模糊写法
     * <p>
     * 识别时间表达式单元的月、日字段
     */
    public int[] normSetmonthFuzzyday(String timeExpression) {
        int monthRes = -1;
        int dayRes = -1;
        String rule = "((10)|(11)|(12)|([1-9]))(月|\\.|\\-)([0-3][0-9]|[1-9])";
        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            String matchStr = match.group();
            Pattern p = Pattern.compile("(月|\\.|\\-)");
            Matcher m = p.matcher(matchStr);
            if (m.find()) {
                int splitIndex = m.start();
                String month = matchStr.substring(0, splitIndex);
                String date = matchStr.substring(splitIndex + 1);

                monthRes = Integer.parseInt(month);
                dayRes = Integer.parseInt(date);

                /**处理倾向于未来时间的情况 */
//				//preferFuture(1);
            }
        }
        return new int[]{monthRes, dayRes};
    }

    /**
     * 日-规范化方法
     * <p>
     * 识别时间表达式单元的日字段
     *
     * @return
     */
    public int normSetday(String timeExpression) {
        int dayRes = -1;
        String rule = "((?<!\\d))([0-3][0-9]|[1-9])(?=(日|号))";
        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            dayRes = Integer.parseInt(match.group());

            /**处理倾向于未来时间的情况 */
//			//preferFuture(2);
        }
        return dayRes;
    }

    /**
     * 时-规范化方法
     * <p>
     * 识别时间表达式单元的时字段
     *
     * @return
     */
    public int normSethour(String timeExpression) {
        int hour = -1;
        String rule = "(?<!(周|星期))([0-2]?[0-9])(?=(点|时))";

        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            hour = Integer.parseInt(match.group());
            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);
        }
        /*
         * 对关键字：早（包含早上/早晨/早间），上午，中午,午间,下午,午后,晚上,傍晚,晚间,晚,pm,PM的正确时间计算
         * 规约：
         * 1.中午/午间0-10点视为12-22点
         * 2.下午/午后0-11点视为12-23点
         * 3.晚上/傍晚/晚间/晚1-11点视为13-23点，12点视为0点
         * 4.0-11点pm/PM视为12-23点
         *
         */
        rule = "凌晨";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            /**增加对没有明确时间点，只写了“凌晨”这种情况的处理*/
            if (hour == -1) {
                hour = RangeTimeEnum.DAY_BREAK.getHourTime();
            }
            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);

        }

        rule = "早上|早晨|早间|晨间|今早|明早";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (hour == -1) /**增加对没有明确时间点，只写了“早上/早晨/早间”这种情况的处理*/ {
                hour = RangeTimeEnum.EARLY_MORNING.getHourTime();
            }
            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);
        }

        rule = "上午";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (hour == -1) /**增加对没有明确时间点，只写了“上午”这种情况的处理*/ {
                hour = RangeTimeEnum.MORNING.getHourTime();
            }
            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);
        }

        rule = "(中午)|(午间)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (hour >= 0 && hour <= 10) {
                hour += 12;
            }
            if (hour == -1) /**增加对没有明确时间点，只写了“中午/午间”这种情况的处理*/ {
                hour = RangeTimeEnum.NOON.getHourTime();
            }
            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);
        }

        rule = "(下午)|(午后)|(pm)|(PM)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (hour >= 0 && hour <= 11) {
                hour += 12;
            }
            if (hour == -1) /**增加对没有明确时间点，只写了“下午|午后”这种情况的处理 */ {
                hour = RangeTimeEnum.AFTERNOON.getHourTime();
            }
            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);
        }

        rule = "晚上|夜间|夜里|今晚|明晚";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (hour >= 1 && hour <= 11) {
                hour += 12;
            } else if (hour == 12) {
                hour = 0;
            } else if (hour == -1) {
                hour = RangeTimeEnum.NIGHT.getHourTime();
            }

            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);
        }
        return hour;
    }

    /**
     * 分-规范化方法
     * <p>
     * 识别时间表达式单元的分字段
     *
     * @return
     */
    public int normSetminute(String timeExpression) {
        int min = -1;
        String rule = "([0-5]?[0-9](?=分(?!钟)))|((?<=((?<!小)[点时]))[0-5]?[0-9](?!刻))";

        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (!match.group().equals("")) {
                min = Integer.parseInt(match.group());
                /**处理倾向于未来时间的情况 */
//				//preferFuture(4);
            }
        }
        /** 加对一刻，半，3刻的正确识别（1刻为15分，半为30分，3刻为45分）*/
        rule = "(?<=[点时])[1一]刻(?!钟)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            min = 15;
            /**处理倾向于未来时间的情况 */
//			//preferFuture(4);
        }

        rule = "(?<=[点时])半";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            min = 30;
            /**处理倾向于未来时间的情况 */
//			//preferFuture(4);
        }

        rule = "(?<=[点时])[3三]刻(?!钟)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            min = 45;
            /**处理倾向于未来时间的情况 */
//			//preferFuture(4);
        }
        return min;
    }

    /**
     * 秒-规范化方法
     * <p>
     * 识别时间表达式单元的秒字段
     *
     * @return
     */
    public int normSetsecond(String timeExpression) {
        int sec = -1;
        /*
         * 添加了省略“分”说法的时间
         * 如17点15分32
         * modified by 曹零
         */
        String rule = "([0-5]?[0-9](?=秒))|((?<=分)[0-5]?[0-9])";

        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            sec = Integer.parseInt(match.group());
        }
        return sec;
    }

    /**
     * 特殊形式的规范化方法
     * <p>
     * 识别特殊形式的时间表达式单元的各个字段
     *
     * @return
     */
    public DateTime normSetTotal(String timeExpression, DateTime dateTime) {
        String rule;
        Pattern pattern;
        Matcher match;
        String[] tmp_parser;
        String tmp_target;

        rule = "(?<!(周|星期))([0-2]?[0-9]):[0-5]?[0-9]:[0-5]?[0-9]";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            tmp_parser = new String[3];
            tmp_target = match.group();
            tmp_parser = tmp_target.split(":");
            dateTime.setHour(Integer.parseInt(tmp_parser[0]));
            dateTime.setMin(Integer.parseInt(tmp_parser[1]));
            dateTime.setSec(Integer.parseInt(tmp_parser[2]));
            /**处理倾向于未来时间的情况 */
//			//preferFuture(3);
        } else {
            rule = "(?<!(周|星期))([0-2]?[0-9]):[0-5]?[0-9]";
            pattern = Pattern.compile(rule);
            match = pattern.matcher(timeExpression);
            if (match.find()) {
                tmp_parser = new String[2];
                tmp_target = match.group();
                tmp_parser = tmp_target.split(":");
                dateTime.setHour(Integer.parseInt(tmp_parser[0]));
                dateTime.setMin(Integer.parseInt(tmp_parser[1]));
                /**处理倾向于未来时间的情况 */
//                preferFuture(3);
            }
        }
        /*
         * 增加了:固定形式时间表达式的
         * 中午,午间,下午,午后,晚上,傍晚,晚间,晚,pm,PM
         * 的正确时间计算，规约同上
         */
        rule = "(中午)|(午间)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (dateTime.getHour() >= 0 && dateTime.getHour() <= 10) {
                dateTime.setHour(dateTime.getHour() + 12);
            }
            if (dateTime.getHour() == -1) /**增加对没有明确时间点，只写了“中午/午间”这种情况的处理*/ {
                dateTime.setHour(RangeTimeEnum.NOON.getHourTime());
            }
            /**处理倾向于未来时间的情况 */
            ////preferFuture(3);

        }

        rule = "(下午)|(午后)|(pm)|(PM)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (dateTime.getHour() >= 0 && dateTime.getHour() <= 11) {
                dateTime.setHour(dateTime.getHour() + 12);
            }
            if (dateTime.getHour() == -1) /**增加对没有明确时间点，只写了“中午/午间”这种情况的处理*/ {
                dateTime.setHour(RangeTimeEnum.AFTERNOON.getHourTime());
            }
            /**处理倾向于未来时间的情况 */
            //preferFuture(3);
        }

        rule = "晚";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            if (dateTime.getHour() >= 1 && dateTime.getHour() <= 11) {
                dateTime.setHour(dateTime.getHour() + 12);
            } else if (dateTime.getHour() == 12) {
                dateTime.setHour(0);
            }
            if (dateTime.getHour() == -1) /**增加对没有明确时间点，只写了“中午/午间”这种情况的处理*/ {
                dateTime.setHour(RangeTimeEnum.NIGHT.getHourTime());
            }
            /**处理倾向于未来时间的情况 */
            //preferFuture(3);
        }


        rule = "[0-9]?[0-9]?[0-9]{2}-((10)|(11)|(12)|([1-9]))-((?<!\\d))([0-3][0-9]|[1-9])";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            tmp_parser = new String[3];
            tmp_target = match.group();
            tmp_parser = tmp_target.split("-");
            dateTime.setYear(Integer.parseInt(tmp_parser[0]));
            dateTime.setMonth(Integer.parseInt(tmp_parser[1]));
            dateTime.setDay(Integer.parseInt(tmp_parser[2]));

        }

        rule = "((10)|(11)|(12)|([1-9]))/((?<!\\d))([0-3][0-9]|[1-9])/[0-9]?[0-9]?[0-9]{2}";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            tmp_parser = new String[3];
            tmp_target = match.group();
            tmp_parser = tmp_target.split("/");
            dateTime.setYear(Integer.parseInt(tmp_parser[0]));
            dateTime.setMonth(Integer.parseInt(tmp_parser[1]));
            dateTime.setDay(Integer.parseInt(tmp_parser[2]));
        }

        /*
         * 增加了:固定形式时间表达式 年.月.日 的正确识别
         * add by 曹零
         */
        rule = "[0-9]?[0-9]?[0-9]{2}\\.((10)|(11)|(12)|([1-9]))\\.((?<!\\d))([0-3][0-9]|[1-9])";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            tmp_parser = new String[3];
            tmp_target = match.group();
            tmp_parser = tmp_target.split("\\.");
            dateTime.setYear(Integer.parseInt(tmp_parser[0]));
            dateTime.setMonth(Integer.parseInt(tmp_parser[1]));
            dateTime.setDay(Integer.parseInt(tmp_parser[2]));
        }

        return dateTime;
    }

    /**
     * 设置以上文时间为基准的时间偏移计算
     *
     * @return
     */
    public DateTime timeOffsetCalc(String baseTime, String timeExpression, DateTime dateTime) {
        Integer[] intBaseTime = Arrays.stream(baseTime.split("-")).map(Integer::parseInt).toArray(Integer[]::new);

        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(intBaseTime[0], intBaseTime[1] - 1, intBaseTime[2], intBaseTime[3], intBaseTime[4], intBaseTime[5]);
        calendar.getTime();

        //观察时间表达式是否因当前相关时间表达式而改变时间,年月日
        boolean[] flag = {false, false, false};

        String rule = "\\d+(?=天[以之]?前)";
        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            int day = Integer.parseInt(match.group());
            calendar.add(Calendar.DATE, -day);
        }

        rule = "\\d+(?=天[以之]?后)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            int day = Integer.parseInt(match.group());
            calendar.add(Calendar.DATE, day);
        }

        rule = "\\d+(?=(个)?月[以之]?前)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[1] = true;
            int month = Integer.parseInt(match.group());
            calendar.add(Calendar.MONTH, -month);
        }

        rule = "\\d+(?=(个)?月[以之]?后)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[1] = true;
            int month = Integer.parseInt(match.group());
            calendar.add(Calendar.MONTH, month);
        }

        rule = "\\d+(?=年[以之]?前)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[0] = true;
            int year = Integer.parseInt(match.group());
            calendar.add(Calendar.YEAR, -year);
        }

        rule = "\\d+(?=年[以之]?后)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[0] = true;
            int year = Integer.parseInt(match.group());
            calendar.add(Calendar.YEAR, year);
        }

        String s = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(calendar.getTime());
        String[] time_fin = s.split("-");
        if (flag[0] || flag[1] || flag[2]) {
            dateTime.setYear(Integer.parseInt(time_fin[0]));
        }
        if (flag[1] || flag[2]) {
            dateTime.setMonth(Integer.parseInt(time_fin[1]));
        }
        if (flag[2]) {
            dateTime.setDay(Integer.parseInt(time_fin[2]));
        }

        return dateTime;
    }

    /**
     * 设置当前时间相关的时间表达式
     *
     * @return
     */
    public DateTime normSetCurRelated(String baseTime, String timeExpression, DateTime dateTime) {
        Integer[] ini = Arrays.stream(baseTime.split("-")).map(Integer::parseInt).toArray(Integer[]::new);
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(ini[0], ini[1] - 1, ini[2], ini[3], ini[4], ini[5]);
        calendar.getTime();
        //观察时间表达式是否因当前相关时间表达式而改变时间
        boolean[] flag = {false, false, false};
        String rule = "前年";
        Pattern pattern = Pattern.compile(rule);
        Matcher match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[0] = true;
            calendar.add(Calendar.YEAR, -2);
        }

        rule = "去年";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[0] = true;
            calendar.add(Calendar.YEAR, -1);
        }

        rule = "今年";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[0] = true;
            calendar.add(Calendar.YEAR, 0);
        }

        rule = "明年";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[0] = true;
            calendar.add(Calendar.YEAR, 1);
        }

        rule = "后年";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[0] = true;
            calendar.add(Calendar.YEAR, 2);
        }

        rule = "上(个)?月";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[1] = true;
            calendar.add(Calendar.MONTH, -1);

        }

        rule = "(本|这个)月";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[1] = true;
            calendar.add(Calendar.MONTH, 0);
        }

        rule = "下(个)?月";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[1] = true;
            calendar.add(Calendar.MONTH, 1);
        }

        rule = "大前天";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            calendar.add(Calendar.DATE, -3);
        }

        rule = "(?<!大)前天";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            calendar.add(Calendar.DATE, -2);
        }

        rule = "昨";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            calendar.add(Calendar.DATE, -1);
        }

        rule = "今(?!年)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            calendar.add(Calendar.DATE, 0);
        }

        rule = "明(?!年)";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            calendar.add(Calendar.DATE, 1);
        }

        rule = "(?<!大)后天";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            calendar.add(Calendar.DATE, 2);
        }

        rule = "大后天";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            calendar.add(Calendar.DATE, 3);
        }

        rule = "(?<=(上上(周|星期|礼拜)))[1-7]";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            int week = Integer.parseInt(match.group());
            if (week == 7) {
                week = 1;
            } else {
                week++;
            }
            calendar.add(Calendar.WEEK_OF_MONTH, -2);
            calendar.set(Calendar.DAY_OF_WEEK, week);
        }

        rule = "(?<=((?<!上)上(周|星期)))[1-7]";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            int week = Integer.parseInt(match.group());
            if (week == 7) {
                week = 1;
            } else {
                week++;
            }
            calendar.add(Calendar.WEEK_OF_MONTH, -1);
            calendar.set(Calendar.DAY_OF_WEEK, week);
        }

        rule = "(?<=((?<!下)下(周|星期)))[1-7]";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            int week = Integer.parseInt(match.group());
            if (week == 7) {
                week = 1;
            } else {
                week++;
            }
            calendar.add(Calendar.WEEK_OF_MONTH, 1);
            calendar.set(Calendar.DAY_OF_WEEK, week);
        }

        rule = "(?<=(下下(周|星期)))[1-7]";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            int week = Integer.parseInt(match.group());
            if (week == 7) {
                week = 1;
            } else {
                week++;
            }
            calendar.add(Calendar.WEEK_OF_MONTH, 2);
            calendar.set(Calendar.DAY_OF_WEEK, week);
        }

        rule = "(?<=((?<!(上|下))(周|星期)))[1-7]";
        pattern = Pattern.compile(rule);
        match = pattern.matcher(timeExpression);
        if (match.find()) {
            flag[2] = true;
            int week = Integer.parseInt(match.group());
            if (week == 7) {
                week = 1;
            } else {
                week++;
            }
            calendar.add(Calendar.WEEK_OF_MONTH, 0);
            calendar.set(Calendar.DAY_OF_WEEK, week);
            /**处理未来时间倾向*/
            //preferFutureWeek(week, calendar);
        }
        String s = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(calendar.getTime());
        String[] time_fin = s.split("-");
        if (flag[0] || flag[1] || flag[2]) {
            dateTime.setYear(Integer.parseInt(time_fin[0]));
        }
        if (flag[1] || flag[2]) {
            dateTime.setMonth(Integer.parseInt(time_fin[1]));
        }
        if (flag[2]) {
            dateTime.setDay(Integer.parseInt(time_fin[2]));
        }
        return dateTime;
    }

    /**
     * 用于更新baseTime使之具有上下文关联性
     */
//    public void modifyTimeBase(String baseTime, String timeExpression, DateTime dateTime) {
//        String[] timeGrid = baseTime.split("-");
//
//        String s = "";
//        if (timePoint.tunit[0] != -1) {
//            s += Integer.toString(timePoint.tunit[0]);
//        } else {
//            s += timeGrid[0];
//        }
//        for (int i = 1; i < 6; i++) {
//            s += "-";
//            if (timePoint.tunit[i] != -1) {
//                s += Integer.toString(timePoint.tunit[i]);
//            } else {
//                s += timeGrid[i];
//            }
//        }
////        timeParser.setBaseTime(s);
//    }


//    /**
//     * 根据上下文时间补充时间信息
//     */
//    private void checkContextTime(int checkTimeIndex) {
//        for (int i = 0; i < checkTimeIndex; i++) {
//            if (timePoint.tunit[i] == -1 && timePointOrigin.tunit[i] != -1) {
//                timePoint.tunit[i] = timePointOrigin.tunit[i];
//            }
//        }
//        /**在处理小时这个级别时，如果上文时间是下午的且下文没有主动声明小时级别以上的时间，则也把下文时间设为下午*/
//        if (isFirstTimeSolveContext && checkTimeIndex == 3 && timePointOrigin.tunit[checkTimeIndex] >= 12 && timePoint.tunit[checkTimeIndex] < 12) {
//            timePoint.tunit[checkTimeIndex] += 12;
//        }
//        isFirstTimeSolveContext = false;
//    }
}
