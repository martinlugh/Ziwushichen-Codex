package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.enums.BaselineEligibilityLevelEnum;
import com.ziwushichen.health.enums.BaselineTypeEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户基线画像实体。
 * 本实体只保存后续流程使用的基线数据，不涉及数据库。
 */
@Data
public class UserBaselineProfile {

    private String userId;

    private BaselineTypeEnum baselineType;

    private BaselineEligibilityLevelEnum baselineEligibilityLevel;

    private LocalDateTime latestRefreshTime;

    /**
     * 时辰维度基线容器。
     */
    private Map<MeridianTimeSlotEnum, BaselineMetricSnapshot> meridianTimeSlotBaselineMap = new ConcurrentHashMap<>();

    /**
     * 基线指标快照。
     */
    @Data
    public static class BaselineMetricSnapshot {
        private Double heartRate;
        private Double spo2;
        private Double sd1;
        private Double sd2;
        private Double hf;
        private Double lf;
        private Double vlf;
        private Double sampleEntropy;
        private Double approximateEntropy;
        private Double dfaAlpha1;
        private Double dfaAlpha2;
        private Double respiratoryRate;
        private Integer stepCount;
        private Double calorieBurn;
        private Integer deviceStressScore;
        private Integer deviceFatigueScore;
        private Integer deviceRecoveryScore;
    }
}
