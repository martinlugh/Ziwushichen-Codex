package com.ziwushichen.health.domain.dto;

import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时辰上下文信息。
 */
@Data
public class MeridianTimeContextDTO {

    private MeridianTimeSlotEnum currentMeridianTimeSlot;

    private MeridianTimeSlotEnum previousMeridianTimeSlot;

    private LocalDateTime slotStartTime;

    private LocalDateTime slotEndTime;

    private long elapsedMinutesInSlot;

    private boolean switched;

    private boolean needSettlePreviousSlot;
}
