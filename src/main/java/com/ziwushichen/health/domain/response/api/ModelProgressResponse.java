package com.ziwushichen.health.domain.response.api;

import com.ziwushichen.health.enums.BaselineModelTypeEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 建模进度响应。
 */
@Data
public class ModelProgressResponse {

    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;

    private Integer candidateCount;

    private Integer eligibleCount;

    private BaselineModelTypeEnum currentType;

    private String progressRemark;

    private LocalDateTime updateTime;
}
