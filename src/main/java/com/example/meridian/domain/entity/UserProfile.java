package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
/** UserProfile实体 */
@Data
@TableName("user_profile")
public class UserProfile {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String userId;
    private Integer age;
    private String gender;
    private Integer heightCm;
    private BigDecimal weightKg;
    private String timezone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
