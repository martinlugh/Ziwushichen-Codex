package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
/** BaselineCandidateSegment实体 */
@Data
@TableName("baseline_candidate_segment")
public class BaselineCandidateSegment {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private String meridianSlotCode;
    private LocalDate segmentDate;
    private String eligibilityLevel;
    private BigDecimal qualityScore;
    private String rejectReason;
    private LocalDateTime createdAt;
}
