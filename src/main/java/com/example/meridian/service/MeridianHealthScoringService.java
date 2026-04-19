package com.example.meridian.service;

import com.example.meridian.domain.dto.MeridianInputDto;
import com.example.meridian.domain.entity.UserMeridianBaseline;
import com.example.meridian.domain.vo.ActivitySceneResultVo;
import com.example.meridian.domain.vo.MeridianWindowVo;
import com.example.meridian.domain.vo.RuleThresholdVo;
import com.example.meridian.domain.vo.ScoringResultVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

/** 评分服务 */
@Service
public class MeridianHealthScoringService {
    public ScoringResultVo score(MeridianInputDto in, MeridianWindowVo window, ActivitySceneResultVo scene, UserMeridianBaseline baseline, RuleThresholdVo rules){
        ScoringResultVo s=new ScoringResultVo();
        BigDecimal oxygen=clamp(new BigDecimal("80").add(in.getSpo2().subtract(rules.getSpo2Low()).multiply(new BigDecimal("3"))));
        BigDecimal load=clamp(new BigDecimal("100").subtract(in.getDeviceStressScore().multiply(new BigDecimal("0.4")).add(in.getDeviceFatigueScore().multiply(new BigDecimal("0.3"))));
        if("ACTIVE".equals(scene.getActivityScene())) load=clamp(load.subtract(new BigDecimal("8")));
        BigDecimal recovery=clamp(in.getDeviceRecoveryScore());
        BigDecimal rhythm=clamp(new BigDecimal("70").add(BigDecimal.valueOf(Math.min(30,window.getWindowMinutes())).multiply(new BigDecimal("1.0"))));
        BigDecimal dev=baseline==null?new BigDecimal("60"):clamp(new BigDecimal("100").subtract(BigDecimal.valueOf(in.getHeartRate()).subtract(baseline.getBaselineHr()).abs()));
        BigDecimal conf=clamp(new BigDecimal("40").add(BigDecimal.valueOf(Math.min(30,window.getWindowMinutes())).multiply(new BigDecimal("1.2"))));
        BigDecimal overall=clamp(
                oxygen.multiply(new BigDecimal("0.18"))
                        .add(load.multiply(new BigDecimal("0.18")))
                        .add(recovery.multiply(new BigDecimal("0.18")))
                        .add(rhythm.multiply(new BigDecimal("0.16")))
                        .add(dev.multiply(new BigDecimal("0.18")))
                        .add(conf.multiply(new BigDecimal("0.12")))
        );
        s.setOxygenScore(r(oxygen)); s.setLoadScore(r(load)); s.setRecoveryHealthScore(r(recovery)); s.setRhythmMatchScore(r(rhythm)); s.setBaselineDeviationScore(r(dev)); s.setConfidenceScore(r(conf)); s.setOverallScore(r(overall));
        s.setMainDrivers(new ArrayList<>()); if(s.getLoadScore().doubleValue()<70) s.getMainDrivers().add("压力疲劳负荷偏高"); if(s.getOxygenScore().doubleValue()<70) s.getMainDrivers().add("血氧状态需要关注"); if(s.getMainDrivers().isEmpty()) s.getMainDrivers().add("整体处于稳定区间");
        return s;
    }
    private BigDecimal clamp(BigDecimal v){ if(v.compareTo(BigDecimal.ZERO)<0)return BigDecimal.ZERO; if(v.compareTo(new BigDecimal("100"))>0)return new BigDecimal("100"); return v; }
    private BigDecimal r(BigDecimal v){ return v.setScale(2, RoundingMode.HALF_UP); }
}
