package cn.mirrorming.text2date;

import cn.mirrorming.text2date.domain.TextContent;
import cn.mirrorming.text2date.handler.HandlerChain;
import cn.mirrorming.text2date.handler.TextPreHandler;
import cn.mirrorming.text2date.handler.TimeContextHandler;
import cn.mirrorming.text2date.handler.TimeWordsParseHandler;

/**
 * @author Mireal Chen
 */
public class TestText2Date {

    public static void main(String[] args) {
//        TextContent textContent = new TextContent("提醒我明天下午八点到九点去北京开会");
//        TextContent textContent = new TextContent("提醒我半小时以后去北京开会");
        TextContent textContent = new TextContent("明天晚上八点去北京开会");
        HandlerChain handlerChain = new HandlerChain();
        handlerChain.add(new TextPreHandler()).add(new TimeWordsParseHandler()).add(new TimeContextHandler());
        Boolean handler = handlerChain.handler(textContent);
        System.out.println(textContent.getResultTime());
    }
}
