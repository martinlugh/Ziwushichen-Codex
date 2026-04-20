package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.enums.BaselineModelTypeEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 建模进度实体。
 */
@Data
public class BaselineModelProgress {

    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;

    private int candidateCount;

    private int eligibleCount;

    private BaselineModelTypeEnum currentType;

    private String progressRemark;

    private LocalDateTime updateTime;
}
