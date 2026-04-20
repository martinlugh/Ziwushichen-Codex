package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.enums.ActivitySceneTypeEnum;
import com.ziwushichen.health.enums.BaselineEligibilityLevelEnum;
import com.ziwushichen.health.enums.BaselineModelTypeEnum;
import com.ziwushichen.health.enums.ConfidenceLevelEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 基线候选片段实体。
 */
@Data
public class BaselineCandidateSegment {

    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;

    private LocalDateTime segmentStartTime;

    private LocalDateTime segmentEndTime;

    private List<WearableMetricSnapshot> metricSnapshotList;

    private ActivitySceneTypeEnum activityScene;

    private BaselineEligibilityLevelEnum baselineEligibilityLevel;

    private Double baselineHealthScore;

    private List<String> baselineRejectReasons;

    private BaselineModelTypeEnum baselineTypeUsed;

    private ConfidenceLevelEnum confidenceLevel;
}
