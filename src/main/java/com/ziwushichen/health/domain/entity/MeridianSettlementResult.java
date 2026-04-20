package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 时辰结算结果实体。
 */
@Data
public class MeridianSettlementResult {

    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;

    private LocalDateTime slotStartTime;

    private LocalDateTime slotEndTime;

    private int sampleCount;

    private String settlementSummary;

    private LocalDateTime settlementTime;
}
