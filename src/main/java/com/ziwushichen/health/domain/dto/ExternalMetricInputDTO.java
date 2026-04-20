package com.ziwushichen.health.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 外部算法结果输入数据传输对象。
 * 本对象字段全部为外部算法直接输出，本系统只消费不重复计算。
 */
@Data
public class ExternalMetricInputDTO {

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

    @NotNull(message = "measureTime不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;
}
