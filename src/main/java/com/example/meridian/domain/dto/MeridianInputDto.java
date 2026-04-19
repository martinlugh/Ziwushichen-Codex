package com.example.meridian.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/** 输入DTO */
@Data
public class MeridianInputDto {
    @NotNull private Integer heartRate;
    @NotNull private BigDecimal spo2;
    @NotNull private List<Integer> rrIntervals;
    @NotNull private String emotionState;
    @NotNull private BigDecimal deviceStressScore;
    @NotNull private BigDecimal deviceFatigueScore;
    @NotNull private BigDecimal deviceRecoveryScore;
    @NotNull private Integer stepCount;
    @NotNull private BigDecimal calorieBurn;
    @NotNull private LocalDateTime measureTime;
}
