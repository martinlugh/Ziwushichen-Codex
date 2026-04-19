package com.example.meridian.service;

import com.example.meridian.domain.entity.DeviceActivityRaw;
import com.example.meridian.domain.vo.ActivitySceneResultVo;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

/** 活动场景服务 */
@Service
public class ActivitySceneService {
    public ActivitySceneResultVo detectScene(List<DeviceActivityRaw> activities, LocalDateTime measureTime){
        ActivitySceneResultVo vo=new ActivitySceneResultVo();
        if(activities==null||activities.isEmpty()){ vo.setActivityScene("REST"); vo.setSceneReason("无活动数据"); return vo; }
        int steps=activities.stream().mapToInt(a->a.getStepCount()==null?0:a.getStepCount()).sum();
        BigDecimal cal=activities.stream().map(a->a.getCalorieBurn()==null?BigDecimal.ZERO:a.getCalorieBurn()).reduce(BigDecimal.ZERO,BigDecimal::add);
        DeviceActivityRaw latest=activities.stream().max(Comparator.comparing(DeviceActivityRaw::getMeasureTime)).orElse(null);
        if(steps<300&&cal.compareTo(new BigDecimal("35"))<0){
            if(latest!=null&&Duration.between(latest.getMeasureTime(),measureTime).toMinutes()<=30){ vo.setActivityScene("POST_ACTIVITY_RECOVERY"); vo.setSceneReason("近期活动后恢复"); }
            else { vo.setActivityScene("REST"); vo.setSceneReason("静息"); }
            return vo;
        }
        if(steps<2500&&cal.compareTo(new BigDecimal("180"))<0){ vo.setActivityScene("LIGHT_ACTIVITY"); vo.setSceneReason("轻活动"); return vo; }
        vo.setActivityScene("ACTIVE"); vo.setSceneReason("活跃活动"); return vo;
    }
}
