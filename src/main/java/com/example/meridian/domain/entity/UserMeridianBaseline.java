package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** UserMeridianBaseline实体 */
@Data
@TableName("user_meridian_baseline")
public class UserMeridianBaseline {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private String baselineType;
    private String meridianSlotCode;
    private BigDecimal baselineHr;
    private BigDecimal baselineSpo2;
    private BigDecimal baselineStress;
    private BigDecimal baselineFatigue;
    private BigDecimal baselineRecovery;
    private String confidenceLevel;
    private BigDecimal confidenceScore;
    private LocalDateTime effectiveAt;
    private LocalDateTime updatedAt;
}
