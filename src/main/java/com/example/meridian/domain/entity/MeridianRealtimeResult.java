package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** MeridianRealtimeResult实体 */
@Data
@TableName("meridian_realtime_result")
public class MeridianRealtimeResult {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private LocalDateTime measureTime;
    private String meridianSlotCode;
    private BigDecimal overallScore;
    private BigDecimal loadScore;
    private BigDecimal recoveryHealthScore;
    private BigDecimal oxygenScore;
    private BigDecimal rhythmMatchScore;
    private BigDecimal baselineDeviationScore;
    private BigDecimal confidenceScore;
    private String healthColorStatus;
    private String mainDriversJson;
    private String colorChangeReason;
    private String medicalSummary;
    private String tcmMeridianSummary;
    private String actionAdviceJson;
    private LocalDateTime createdAt;
}
