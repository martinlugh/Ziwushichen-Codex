package com.example.meridian.service;

import com.example.meridian.domain.dto.MeridianInputDto;
import com.example.meridian.domain.entity.MeridianRealtimeResult;
import com.example.meridian.domain.vo.ColorDecisionVo;
import com.example.meridian.domain.vo.RuleThresholdVo;
import com.example.meridian.domain.vo.ScoringResultVo;
import com.example.meridian.mapper.MeridianRealtimeResultMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/** 颜色决策服务 */
@Service
@RequiredArgsConstructor
public class HealthColorDecisionService {
    private final MeridianRealtimeResultMapper mapper;
    public ColorDecisionVo decide(String userId, MeridianInputDto input, ScoringResultVo score, RuleThresholdVo rules, boolean reachedMinWindow){
        ColorDecisionVo vo=new ColorDecisionVo();
        if(!reachedMinWindow){ vo.setHealthColorStatus("DATA_INSUFFICIENT"); vo.setColorChangeReason("窗口不足"); return vo; }
        if(BigDecimal.valueOf(input.getHeartRate()).compareTo(rules.getCriticalHrHigh())>0 || input.getSpo2().compareTo(rules.getCriticalSpo2Low())<0){
            vo.setHealthColorStatus("IMBALANCE_ALERT"); vo.setColorChangeReason("关键异常一票否决，仅用于失衡预警"); return vo;
        }
        String base=score.getOverallScore().compareTo(new BigDecimal("80"))>=0?"HEALTHY":score.getOverallScore().compareTo(new BigDecimal("60"))>=0?"SUB_HEALTH":"IMBALANCE_ALERT";
        MeridianRealtimeResult prev=mapper.selectLatestByUser(userId);
        if(prev!=null && isBetter(base, prev.getHealthColorStatus()) && score.getOverallScore().compareTo(new BigDecimal("85"))<0){
            vo.setHealthColorStatus(prev.getHealthColorStatus()); vo.setColorChangeReason("抗抖动降慢"); return vo;
        }
        vo.setHealthColorStatus(base); vo.setColorChangeReason("按综合分与持续性判定"); return vo;
    }
    private boolean isBetter(String now,String prev){ return level(now)<level(prev); }
    private int level(String c){ return switch(c){case "HEALTHY"->1;case "SUB_HEALTH"->2;case "IMBALANCE_ALERT"->3;default->4;}; }
}
