package cn.mirrorming.text2date;

import cn.mirrorming.text2date.domain.TextContent;
import cn.mirrorming.text2date.handler.HandlerChain;
import cn.mirrorming.text2date.handler.TextPreHandler;
import cn.mirrorming.text2date.handler.TimeContextHandler;
import cn.mirrorming.text2date.handler.TimeWordsParseHandler;
import lombok.Cleanup;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author Mireal Chen
 */
public class TestParse {
    public static void main(String[] args) {
        String filepath = "src/main/resources/parse/测试用例.txt";
        try {
            // 创建对应f的文件输入流
            @Cleanup FileInputStream fis = new FileInputStream(filepath);
            @Cleanup InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            @Cleanup BufferedReader br = new BufferedReader(isr);
            br.lines().filter(str -> !StringUtils.isEmpty(str)).forEach(a -> {
                System.err.println("文本为----->" + a);


                TextContent textContent = new TextContent(a);
                HandlerChain handlerChain = new HandlerChain();
                handlerChain.add(new TextPreHandler()).add(new TimeWordsParseHandler()).add(new TimeContextHandler());
                Boolean handler = handlerChain.handler(textContent);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
