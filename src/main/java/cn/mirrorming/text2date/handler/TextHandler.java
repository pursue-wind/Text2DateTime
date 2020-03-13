package cn.mirrorming.text2date.handler;

import cn.mirrorming.text2date.domain.TextContent;

/**
 * @author Mireal Chen
 * @version V1.0
 * @date 2020/3/12 20:00
 */
@FunctionalInterface
public interface TextHandler {
    /**
     * 文本处理基础接口
     *
     * @return
     */
    Boolean handler(TextContent textContent);
}
