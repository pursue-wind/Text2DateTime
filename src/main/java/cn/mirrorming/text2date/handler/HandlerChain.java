package cn.mirrorming.text2date.handler;

import cn.mirrorming.text2date.domain.TextContent;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Mireal Chen
 * @version V1.0
 * @date 2020/3/12 21:40
 */
public class HandlerChain implements TextHandler {
    private List<TextHandler> handlers = new LinkedList<>();

    public HandlerChain add(TextHandler textHandler) {
        this.handlers.add(textHandler);
        return this;
    }

    @Override
    public Boolean handler(TextContent textContent) {
        for (TextHandler textHandler : handlers) {
            Boolean handlerRes = textHandler.handler(textContent);
            if (!handlerRes) {
                return false;
            }
        }
        return true;
    }
}
