package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/** MeridianTimeslotSummary实体 */
@Data
@TableName("meridian_timeslot_summary")
public class MeridianTimeslotSummary {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private LocalDate summaryDate;
    private String meridianSlotCode;
    private BigDecimal avgOverallScore;
    private String dominantColorStatus;
    private Integer sampleCount;
    private LocalDateTime createdAt;
}
