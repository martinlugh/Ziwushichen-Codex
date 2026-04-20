package com.ziwushichen.health.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 健康状态判断请求对象。
 * 所有核心指标由外部算法提供，本系统仅做消费和判定。
 */
@Data
public class HealthStateJudgeRequest {

    @NotBlank(message = "userId不能为空")
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

    @NotNull(message = "measureTime不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;
}
