package com.example.meridian.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
/** MeridianRuleConfig实体 */
@Data
@TableName("meridian_rule_config")
public class MeridianRuleConfig {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String ruleCode;
    private String ruleName;
    private String ruleContentJson;
    private Integer enabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
