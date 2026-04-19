package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.MeridianRuleConfig;
import org.apache.ibatis.annotations.Mapper;

/** MeridianRuleConfigMapper */
@Mapper
public interface MeridianRuleConfigMapper extends BaseMapper<MeridianRuleConfig> {
    java.util.List<MeridianRuleConfig> selectEnabledRules();
    MeridianRuleConfig selectEnabledRuleByCode(@org.apache.ibatis.annotations.Param("ruleCode") String ruleCode);
}
