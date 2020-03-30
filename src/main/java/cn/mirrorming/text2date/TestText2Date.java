package cn.mirrorming.text2date;


import cn.mirrorming.text2date.time.TimeEntity;
import cn.mirrorming.text2date.time.TimeEntityRecognizer;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Mireal Chen
 */
public class TestText2Date {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static void main(String[] args) {
        String a = "四个半小时后去福田公园开会";
        try {
            TimeEntityRecognizer timeEntityRecognizer = new TimeEntityRecognizer();
            List<TimeEntity> parse = timeEntityRecognizer.parse(a);
            parse.forEach(p -> {
                Date value = p.getValue();
                System.err.println(sdf.format(value));
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
