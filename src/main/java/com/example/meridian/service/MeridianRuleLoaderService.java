package com.example.meridian.service;

import com.example.meridian.domain.entity.MeridianRuleConfig;
import com.example.meridian.domain.vo.RuleThresholdVo;
import com.example.meridian.mapper.MeridianRuleConfigMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/** 规则加载服务 */
@Service
@RequiredArgsConstructor
public class MeridianRuleLoaderService {
    private final MeridianRuleConfigMapper mapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public RuleThresholdVo loadThresholds(){
        MeridianRuleConfig r=mapper.selectEnabledRuleByCode("DEFAULT_THRESHOLD_RULE");
        if(r==null||r.getRuleContentJson()==null||r.getRuleContentJson().isBlank()) return new RuleThresholdVo();
        try{return objectMapper.readValue(r.getRuleContentJson(),RuleThresholdVo.class);}catch(Exception e){return new RuleThresholdVo();}
    }
    public List<MeridianRuleConfig> listEnabledRules(){ return mapper.selectEnabledRules(); }
}
