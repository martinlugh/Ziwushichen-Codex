package com.ziwushichen.health.domain.request.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 生理数据输入请求。
 */
@Data
public class DeviceVitalsRequest {

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
    private String emotionState;
    private Integer deviceStressScore;
    private Integer deviceFatigueScore;
    private Integer deviceRecoveryScore;

    @NotNull(message = "measureTime不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;
}
