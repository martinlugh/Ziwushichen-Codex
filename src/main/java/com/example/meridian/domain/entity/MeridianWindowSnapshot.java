package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
/** MeridianWindowSnapshot实体 */
@Data
@TableName("meridian_window_snapshot")
public class MeridianWindowSnapshot {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private String meridianSlotCode;
    private LocalDate measureDate;
    private String rawVitalIdsJson;
    private String rawActivityIdsJson;
    private Integer sampleCount;
    private LocalDateTime createdAt;
}
