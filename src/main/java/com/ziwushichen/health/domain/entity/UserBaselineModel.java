package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.enums.BaselineModelTypeEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户基线实体。
 */
@Data
public class UserBaselineModel {

    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;

    private BaselineModelTypeEnum baselineType;

    private UserBaselineProfile.BaselineMetricSnapshot baselineMetricSnapshot;

    private LocalDateTime baselineBuildTime;
}
