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
//    public static void main(String[] args) {
//        String filepath = "src/main/resources/parse/测试用例.txt";
//        try {
//            // 创建对应f的文件输入流
//            @Cleanup FileInputStream fis = new FileInputStream(filepath);
//            @Cleanup InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
//            @Cleanup BufferedReader br = new BufferedReader(isr);
//            br.lines().filter(Objects::nonNull).forEach(a -> {
//                System.err.println("文本为----->" + a);
//
//
//                TextContent textContent = new TextContent(a);
//                HandlerChain handlerChain = new HandlerChain();
//                handlerChain.add(new TextPreHandler()).add(new TimeWordsParseHandler()).add(new TimeContextHandler());
//
//                Boolean handler = handlerChain.handler(textContent);
//
//            });
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

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
