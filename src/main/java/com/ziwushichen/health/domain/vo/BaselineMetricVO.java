package com.ziwushichen.health.domain.vo;

import lombok.Data;

/**
 * 基线指标展示对象。
 */
@Data
public class BaselineMetricVO {

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
