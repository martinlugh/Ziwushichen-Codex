package com.example.meridian.service;

import com.example.meridian.common.exception.BizException;
import com.example.meridian.domain.dto.MeridianInputDto;
import com.example.meridian.domain.entity.*;
import com.example.meridian.domain.request.MeridianEvaluateRequest;
import com.example.meridian.domain.response.MeridianEvaluateResponse;
import com.example.meridian.domain.vo.*;
import com.example.meridian.mapper.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** 实时评估服务 */
@Service
@RequiredArgsConstructor
public class MeridianRealtimeEvaluateService {
    private final MeridianTimeEngine timeEngine; private final DeviceVitalRawMapper vitalMapper; private final DeviceActivityRawMapper activityMapper; private final ActivitySceneService sceneService; private final UserMeridianBaselineMapper baselineMapper; private final MeridianRuleLoaderService ruleLoader; private final MeridianHealthScoringService scoringService; private final HealthColorDecisionService colorService; private final MeridianNarrativeService narrativeService; private final MeridianRealtimeResultMapper resultMapper;
    private final ObjectMapper om=new ObjectMapper();
    @Transactional(rollbackFor = Exception.class)
    public MeridianEvaluateResponse evaluate(MeridianEvaluateRequest req){
        if(req==null||req.getInput()==null) throw new BizException("参数不能为空");
        MeridianInputDto in=req.getInput(); MeridianWindowVo window=timeEngine.buildWindow(in.getMeasureTime(),10);
        List<DeviceVitalRaw> v=vitalMapper.selectByUserSlotAndTimeRange(req.getUserId(),window.getMeridianSlotCode(),window.getWindowStartTime(),window.getWindowEndTime());
        List<DeviceActivityRaw> a=activityMapper.selectByUserSlotAndTimeRange(req.getUserId(),window.getMeridianSlotCode(),window.getWindowStartTime(),window.getWindowEndTime());
        ActivitySceneResultVo scene=sceneService.detectScene(a,in.getMeasureTime());
        UserMeridianBaseline baseline=selectBaseline(req.getUserId(),window.getMeridianSlotCode());
        RuleThresholdVo rules=ruleLoader.loadThresholds();
        ScoringResultVo score=scoringService.score(in,window,scene,baseline,rules);
        ColorDecisionVo color=colorService.decide(req.getUserId(),in,score,rules,window.isReachedMinWindow());
        String medical=narrativeService.buildMedicalSummary(score,color.getHealthColorStatus());
        String tcm=narrativeService.buildTcmMeridianSummary(window.getMeridianSlotCode());
        List<String> advice=narrativeService.buildActionAdvice(score,color.getHealthColorStatus());
        MeridianRealtimeResult row=new MeridianRealtimeResult(); row.setUserId(req.getUserId()); row.setMeasureTime(in.getMeasureTime()); row.setMeridianSlotCode(window.getMeridianSlotCode()); row.setOverallScore(score.getOverallScore()); row.setLoadScore(score.getLoadScore()); row.setRecoveryHealthScore(score.getRecoveryHealthScore()); row.setOxygenScore(score.getOxygenScore()); row.setRhythmMatchScore(score.getRhythmMatchScore()); row.setBaselineDeviationScore(score.getBaselineDeviationScore()); row.setConfidenceScore(score.getConfidenceScore()); row.setHealthColorStatus(color.getHealthColorStatus());
        try{ row.setMainDriversJson(om.writeValueAsString(score.getMainDrivers())); row.setActionAdviceJson(om.writeValueAsString(advice)); }catch(Exception e){ row.setMainDriversJson("[]"); row.setActionAdviceJson("[]"); }
        row.setColorChangeReason(color.getColorChangeReason()); row.setMedicalSummary(medical); row.setTcmMeridianSummary(tcm);
        resultMapper.insert(row);
        MeridianEvaluateResponse r=new MeridianEvaluateResponse(); r.setOverallScore(score.getOverallScore()); r.setLoadScore(score.getLoadScore()); r.setRecoveryHealthScore(score.getRecoveryHealthScore()); r.setOxygenScore(score.getOxygenScore()); r.setRhythmMatchScore(score.getRhythmMatchScore()); r.setBaselineDeviationScore(score.getBaselineDeviationScore()); r.setConfidenceScore(score.getConfidenceScore()); r.setHealthColorStatus(color.getHealthColorStatus()); r.setMainDrivers(score.getMainDrivers()); r.setColorChangeReason(color.getColorChangeReason()); r.setMedicalSummary(medical); r.setTcmMeridianSummary(tcm); r.setActionAdvice(advice);
        return r;
    }
    private UserMeridianBaseline selectBaseline(String user,String slot){
        UserMeridianBaseline b=baselineMapper.selectByUserTypeAndSlot(user,"PERSONAL_HEALTHY",slot); if(b!=null) return b;
        b=baselineMapper.selectByUserTypeAndSlot(user,"PERSONAL_STABLE",slot); if(b!=null) return b;
        return baselineMapper.selectByUserTypeAndSlot(user,"GENERAL_REFERENCE",slot);
    }
}
