package com.ziwushichen.health.enums;

import java.time.LocalTime;

/**
 * 固定十二时辰枚举。
 */
public enum MeridianTimeSlotEnum {

    ZI(LocalTime.of(23, 0), LocalTime.of(1, 0)),
    CHOU(LocalTime.of(1, 0), LocalTime.of(3, 0)),
    YIN(LocalTime.of(3, 0), LocalTime.of(5, 0)),
    MAO(LocalTime.of(5, 0), LocalTime.of(7, 0)),
    CHEN(LocalTime.of(7, 0), LocalTime.of(9, 0)),
    SI(LocalTime.of(9, 0), LocalTime.of(11, 0)),
    WU(LocalTime.of(11, 0), LocalTime.of(13, 0)),
    WEI(LocalTime.of(13, 0), LocalTime.of(15, 0)),
    SHEN(LocalTime.of(15, 0), LocalTime.of(17, 0)),
    YOU(LocalTime.of(17, 0), LocalTime.of(19, 0)),
    XU(LocalTime.of(19, 0), LocalTime.of(21, 0)),
    HAI(LocalTime.of(21, 0), LocalTime.of(23, 0));

    private final LocalTime startTime;
    private final LocalTime endTime;

    MeridianTimeSlotEnum(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    /**
     * 判断时间是否在当前时辰范围内。
     * 采用左闭右开区间。
     *
     * @param localTime 时间
     * @return 是否命中
     */
    public boolean contains(LocalTime localTime) {
        if (startTime.isAfter(endTime)) {
            return !localTime.isBefore(startTime) || localTime.isBefore(endTime);
        }
        return !localTime.isBefore(startTime) && localTime.isBefore(endTime);
    }
}
