package com.ziwushichen.health.domain.dto;

import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时辰上下文数据传输对象。
 */
@Data
public class TimeSlotContextDTO {

    private MeridianTimeSlotEnum meridianTimeSlot;

    private LocalDateTime slotStartTime;

    private LocalDateTime slotEndTime;
}
