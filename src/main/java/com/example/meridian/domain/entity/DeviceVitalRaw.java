package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** DeviceVitalRaw实体 */
@Data
@TableName("device_vital_raw")
public class DeviceVitalRaw {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private Integer heartRate;
    private BigDecimal spo2;
    private String rrIntervalsJson;
    private String emotionState;
    private BigDecimal deviceStressScore;
    private BigDecimal deviceFatigueScore;
    private BigDecimal deviceRecoveryScore;
    private LocalDateTime measureTime;
    private String meridianSlotCode;
    private LocalDateTime createdAt;
}
