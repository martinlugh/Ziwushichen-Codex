package com.example.meridian.service;

import com.example.meridian.common.constants.CommonConstants;
import com.example.meridian.domain.entity.DeviceActivityRaw;
import com.example.meridian.domain.entity.DeviceVitalRaw;
import com.example.meridian.domain.vo.BaselineBuildResultVo;
import com.example.meridian.domain.vo.BaselineFilterResultVo;
import com.example.meridian.mapper.DeviceActivityRawMapper;
import com.example.meridian.mapper.DeviceVitalRawMapper;
import com.example.meridian.mapper.MeridianModelProgressMapper;
import com.example.meridian.mapper.UserMeridianBaselineMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

/** 基线构建服务 */
@Service
@RequiredArgsConstructor
public class MeridianBaselineBuildService {
    private final DeviceVitalRawMapper vitalMapper; private final DeviceActivityRawMapper activityMapper; private final UserMeridianBaselineMapper baselineMapper; private final MeridianModelProgressMapper progressMapper; private final BaselineHealthFilterService filterService;
    @Transactional(rollbackFor = Exception.class)
    public BaselineBuildResultVo build(String userId,String slot,LocalDateTime now){
        List<DeviceVitalRaw> vitals=vitalMapper.selectRecent3DaysSameSlot(userId,slot,now.minusDays(3),now);
        List<DeviceActivityRaw> acts=activityMapper.selectRecent3DaysSameSlot(userId,slot,now.minusDays(3),now);
        BaselineBuildResultVo r=new BaselineBuildResultVo(); r.setUserId(userId); r.setMeridianSlotCode(slot); r.setCandidateCount(vitals.size()); r.setHealthyCount(0);
        if(vitals.isEmpty()) return r;
        BaselineFilterResultVo f=filterService.filter(vitals,acts,slot);
        upsert(userId,"GENERAL_REFERENCE",slot,vitals,f.getConfidenceLevel(),new BigDecimal("60")); r.setGeneralReferenceUpdated(true);
        if("STRONG".equals(f.getBaselineEligibilityLevel())){ upsert(userId,"PERSONAL_HEALTHY",slot,vitals,"HIGH",new BigDecimal("90")); r.setPersonalHealthyUpdated(true); }
        if("PASS".equals(f.getBaselineEligibilityLevel())||"STRONG".equals(f.getBaselineEligibilityLevel())){ upsert(userId,"PERSONAL_STABLE",slot,vitals,f.getConfidenceLevel(),new BigDecimal("80")); r.setPersonalStableUpdated(true); }
        progressMapper.upsertProgress(userId,1, CommonConstants.FIXED_MERIDIAN_SLOT_COUNT,new BigDecimal("8.33"),"基线构建完成");
        r.setHealthyCount(vitals.size()); return r;
    }
    private void upsert(String userId,String type,String slot,List<DeviceVitalRaw> list,String confLevel,BigDecimal confScore){
        baselineMapper.upsertBaseline(userId,type,slot,avg(list.stream().map(v->BigDecimal.valueOf(v.getHeartRate())).toList()),avg(list.stream().map(DeviceVitalRaw::getSpo2).toList()),avg(list.stream().map(DeviceVitalRaw::getDeviceStressScore).toList()),avg(list.stream().map(DeviceVitalRaw::getDeviceFatigueScore).toList()),avg(list.stream().map(DeviceVitalRaw::getDeviceRecoveryScore).toList()),confLevel,confScore);
    }
    private BigDecimal avg(List<BigDecimal> l){ if(l==null||l.isEmpty()) return BigDecimal.ZERO; return l.stream().reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(l.size()),2, RoundingMode.HALF_UP); }
}
