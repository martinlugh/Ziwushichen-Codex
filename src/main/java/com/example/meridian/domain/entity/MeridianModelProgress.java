package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** MeridianModelProgress实体 */
@Data
@TableName("meridian_model_progress")
public class MeridianModelProgress {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private Integer completedSlots;
    private Integer totalSlots;
    private BigDecimal progressPercent;
    private String progressNote;
    private LocalDateTime updatedAt;
}
