package cn.mirrorming.text2date;

import cn.mirrorming.text2date.domain.TextContent;
import cn.mirrorming.text2date.handler.HandlerChain;
import cn.mirrorming.text2date.handler.TextPreHandler;
import cn.mirrorming.text2date.handler.TimeContextHandler;
import cn.mirrorming.text2date.handler.TimeWordsParseHandler;

import java.text.SimpleDateFormat;

/**
 * @author Mireal Chen
 */
public class TestText2Date {
    public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static void main(String[] args) {


        TextContent textContent = new TextContent("提醒我明天下午八点到十二点去北京开会");
//        TextContent textContent = new TextContent("提醒我半小时以后去北京开会");
//        TextContent textContent = new TextContent("明天晚上八点去北京开会");
        HandlerChain handlerChain = new HandlerChain();
        handlerChain.add(new TextPreHandler()).add(new TimeWordsParseHandler()).add(new TimeContextHandler());
        handlerChain.handler(textContent);
        textContent.getResultTime().forEach(c -> {
            String format = sdf.format(c);
            System.err.println("------------------>" + format);
        });
        System.out.println(textContent.getResultTime());
    }
}
