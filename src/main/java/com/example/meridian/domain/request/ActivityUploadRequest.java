package com.example.meridian.domain.request;
import jakarta.validation.constraints.*;import lombok.Data;import java.math.BigDecimal;import java.time.LocalDateTime;
/** 活动上传请求 */
@Data public class ActivityUploadRequest { @NotBlank private String userId; @NotNull private Integer stepCount; @NotNull private BigDecimal calorieBurn; private String activityScene; @NotNull private LocalDateTime measureTime; }
