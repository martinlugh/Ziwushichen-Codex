package com.example.meridian.domain.request;
import jakarta.validation.constraints.*;import lombok.Data;import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.List;
/** 生理上传请求 */
@Data public class VitalUploadRequest { @NotBlank private String userId; @NotNull private Integer heartRate; @NotNull private BigDecimal spo2; @NotNull private List<Integer> rrIntervals; @NotBlank private String emotionState; @NotNull private BigDecimal deviceStressScore; @NotNull private BigDecimal deviceFatigueScore; @NotNull private BigDecimal deviceRecoveryScore; @NotNull private LocalDateTime measureTime; }
