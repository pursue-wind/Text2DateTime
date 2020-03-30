package cn.mirrorming.text2date;


import cn.mirrorming.text2date.time.TimeEntity;
import cn.mirrorming.text2date.time.TimeEntityRecognizer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Mireal Chen
 */
public class TestText2Date2 {
    private static final Pattern R = Pattern.compile("(\\d?+(?=(个)?+半+(个)?+小时[以之]?后))");

    public static void main(String[] args) {
        String a = "2个半小时后";
        Matcher matcher = R.matcher(a);
        if (matcher.find()) {
            System.out.println(matcher.group());
        }

    }
}
