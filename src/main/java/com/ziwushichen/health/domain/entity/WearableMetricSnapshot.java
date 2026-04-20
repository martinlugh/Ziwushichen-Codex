package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 穿戴设备指标快照实体。
 * 所有指标均来自外部算法输出，本实体仅用于内存存储。
 */
@Data
public class WearableMetricSnapshot {

    private String userId;

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
    private String emotionState;
    private Integer deviceStressScore;
    private Integer deviceFatigueScore;
    private Integer deviceRecoveryScore;
    private LocalDateTime measureTime;

    /**
     * 数据所属时辰。
     */
    private MeridianTimeSlotEnum meridianTimeSlot;
}
