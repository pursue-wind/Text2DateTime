package cn.mirrorming.text2date;


import cn.mirrorming.text2date.time.TimeEntity;
import cn.mirrorming.text2date.time.TimeEntityRecognizer;
import lombok.Cleanup;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Mireal Chen
 */
public class TestParse {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static void main(String[] args) {
        String filepath = "src/main/resources/parse/测试用例.txt";
        try {
            // 创建对应f的文件输入流
            @Cleanup FileInputStream fis = new FileInputStream(filepath);
            @Cleanup InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            @Cleanup BufferedReader br = new BufferedReader(isr);
            br.lines().filter(str -> !StringUtils.isEmpty(str)).forEach(a -> {
                System.err.println("文本为----->" + a);
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
//
//                TextContent textContent = new TextContent(a);
//                HandlerChain handlerChain = new HandlerChain();
//                handlerChain.add(new TextPreHandler()).add(new TimeWordsParseHandler()).add(new TimeContextHandler());
//                Boolean handler = handlerChain.handler(textContent);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
