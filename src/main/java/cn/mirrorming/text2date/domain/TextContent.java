package cn.mirrorming.text2date.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Mireal Chen
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextContent implements Serializable {
    /**
     * 当前时间或者传入时间
     */
    private String curTime;
    /**
     * 参考时间，后一个时间只有X点X分  但没有天的时候可以参照
     */
    private String referenceTime;
    /**
     * 解析的目标文本
     */
    private String target;
    private List<Date> resultTime;
    private List<String> tempList;
    /**
     * 是否倾向于未来时间
     */
    private boolean isPreferFuture = true;

    public TextContent(String target) {
        //必须以这种格式，用于分割时间
        this.curTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
        this.target = target;
    }

    public TextContent(String target, Date curTime) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(curTime);
        this.curTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(cal);
        this.target = target;
    }
}

