package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** DeviceActivityRaw实体 */
@Data
@TableName("device_activity_raw")
public class DeviceActivityRaw {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private Integer stepCount;
    private BigDecimal calorieBurn;
    private String activityScene;
    private LocalDateTime measureTime;
    private String meridianSlotCode;
    private LocalDateTime createdAt;
}
