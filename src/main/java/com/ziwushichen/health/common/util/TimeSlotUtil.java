package com.ziwushichen.health.common.util;

import com.ziwushichen.health.enums.MeridianTimeSlotEnum;

import java.time.LocalTime;

/**
 * 时辰工具类。
 */
public final class TimeSlotUtil {

    private TimeSlotUtil() {
    }

    /**
     * 根据时间匹配固定时辰。
     *
     * @param localTime 当前时间
     * @return 对应时辰
     */
    public static MeridianTimeSlotEnum resolveMeridianTimeSlot(LocalTime localTime) {
        for (MeridianTimeSlotEnum timeSlotEnum : MeridianTimeSlotEnum.values()) {
            if (timeSlotEnum.contains(localTime)) {
                return timeSlotEnum;
            }
        }
        throw new IllegalArgumentException("无法匹配时辰");
    }
}
