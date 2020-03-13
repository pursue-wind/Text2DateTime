package cn.mirrorming.text2date.domain;

import lombok.Getter;
import lombok.Setter;

public enum RangeTimeEnum {

    DAY_BREAK(3),
    MORNING(10),
    NOON(12),
    AFTERNOON(15),
    NIGHT(18),
    LATE_NIGHT(20),
    MID_NIGHT(23),
    EARLY_MORNING(8);

    @Setter
    @Getter
    int hourTime = 0;

    RangeTimeEnum(int hourTime) {
        this.setHourTime(hourTime);
    }
}
