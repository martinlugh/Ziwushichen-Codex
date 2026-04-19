package com.example.meridian.service;

import com.example.meridian.domain.entity.DeviceActivityRaw;
import com.example.meridian.domain.entity.DeviceVitalRaw;
import com.example.meridian.domain.vo.ActivitySceneResultVo;
import com.example.meridian.domain.vo.BaselineFilterResultVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** 基线健康筛选服务 */
@Service
@RequiredArgsConstructor
public class BaselineHealthFilterService {
    private final ActivitySceneService activitySceneService;
    public BaselineFilterResultVo filter(List<DeviceVitalRaw> vitals,List<DeviceActivityRaw> acts,String slot){
        BaselineFilterResultVo r=new BaselineFilterResultVo();
        if(vitals==null||vitals.isEmpty()){ r.setBaselineEligibilityLevel("REJECT"); r.setBaselineHealthScore(BigDecimal.ZERO); r.getBaselineRejectReasons().add("无生理数据"); r.setBaselineTypeUsed("GENERAL_REFERENCE"); r.setConfidenceLevel("LOW"); return r; }
        BigDecimal score=new BigDecimal("100");
        boolean stable=vitals.stream().mapToInt(DeviceVitalRaw::getHeartRate).average().orElse(0)>=50 && vitals.stream().map(DeviceVitalRaw::getSpo2).mapToDouble(BigDecimal::doubleValue).average().orElse(0)>=94;
        if(!stable){score=score.subtract(new BigDecimal("35")); r.getBaselineRejectReasons().add("生理稳定性不足");}
        boolean sfr=vitals.stream().map(DeviceVitalRaw::getDeviceStressScore).mapToDouble(BigDecimal::doubleValue).average().orElse(0)<=65;
        if(!sfr){score=score.subtract(new BigDecimal("30")); r.getBaselineRejectReasons().add("压力疲劳恢复不达标");}
        ActivitySceneResultVo scene=activitySceneService.detectScene(acts,vitals.get(vitals.size()-1).getMeasureTime());
        if("ACTIVE".equals(scene.getActivityScene())){score=score.subtract(new BigDecimal("20")); r.getBaselineRejectReasons().add("活动干扰偏高");}
        if(vitals.stream().anyMatch(v->!slot.equals(v.getMeridianSlotCode()))){score=score.subtract(new BigDecimal("40")); r.getBaselineRejectReasons().add("存在跨时辰数据");}
        if(score.compareTo(BigDecimal.ZERO)<0) score=BigDecimal.ZERO;
        r.setBaselineHealthScore(score.setScale(2, RoundingMode.HALF_UP));
        r.setBaselineEligibilityLevel(score.compareTo(new BigDecimal("85"))>=0?"STRONG":score.compareTo(new BigDecimal("65"))>=0?"PASS":score.compareTo(new BigDecimal("45"))>=0?"WEAK":"REJECT");
        r.setConfidenceLevel(score.compareTo(new BigDecimal("85"))>=0?"HIGH":score.compareTo(new BigDecimal("65"))>=0?"MEDIUM":"LOW");
        r.setBaselineTypeUsed("STRONG".equals(r.getBaselineEligibilityLevel())?"PERSONAL_HEALTHY":"PASS".equals(r.getBaselineEligibilityLevel())?"PERSONAL_STABLE":"GENERAL_REFERENCE");
        return r;
    }
}
