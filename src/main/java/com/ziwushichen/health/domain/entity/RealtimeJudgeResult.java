package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.domain.response.HealthStateJudgeResponse;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实时结果实体。
 */
@Data
public class RealtimeJudgeResult {

    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;

    private LocalDateTime resultTime;

    private HealthStateJudgeResponse healthStateJudgeResponse;
}
