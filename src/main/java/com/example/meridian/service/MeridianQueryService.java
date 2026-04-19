package com.example.meridian.service;

import com.example.meridian.common.exception.BizException;
import com.example.meridian.domain.dto.MeridianInputDto;
import com.example.meridian.domain.entity.*;
import com.example.meridian.domain.request.MeridianEvaluateRequest;
import com.example.meridian.domain.response.MeridianEvaluateResponse;
import com.example.meridian.domain.vo.DailySummaryVo;
import com.example.meridian.domain.vo.TimeslotSummaryVo;
import com.example.meridian.mapper.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/** 聚合查询服务 */
@Service
@RequiredArgsConstructor
public class MeridianQueryService {
    private final MeridianRealtimeResultMapper resultMapper; private final DeviceVitalRawMapper vitalMapper; private final DeviceActivityRawMapper activityMapper; private final MeridianRealtimeEvaluateService evaluateService; private final MeridianTimeslotSummaryMapper summaryMapper; private final MeridianModelProgressMapper progressMapper;
    private final ObjectMapper om=new ObjectMapper();
    public MeridianEvaluateResponse getCurrentRealtime(String userId){
        MeridianRealtimeResult latest=resultMapper.selectLatestByUser(userId);
        if(latest!=null){ MeridianEvaluateResponse r=new MeridianEvaluateResponse(); r.setOverallScore(latest.getOverallScore()); r.setLoadScore(latest.getLoadScore()); r.setRecoveryHealthScore(latest.getRecoveryHealthScore()); r.setOxygenScore(latest.getOxygenScore()); r.setRhythmMatchScore(latest.getRhythmMatchScore()); r.setBaselineDeviationScore(latest.getBaselineDeviationScore()); r.setConfidenceScore(latest.getConfidenceScore()); r.setHealthColorStatus(latest.getHealthColorStatus()); try{r.setMainDrivers(om.readValue(latest.getMainDriversJson(),new TypeReference<List<String>>(){})); r.setActionAdvice(om.readValue(latest.getActionAdviceJson(),new TypeReference<List<String>>(){}));}catch(Exception e){r.setMainDrivers(List.of()); r.setActionAdvice(List.of());} r.setColorChangeReason(latest.getColorChangeReason()); r.setMedicalSummary(latest.getMedicalSummary()); r.setTcmMeridianSummary(latest.getTcmMeridianSummary()); return r; }
        DeviceVitalRaw v=vitalMapper.selectLatestByUser(userId); DeviceActivityRaw a=activityMapper.selectLatestByUser(userId); if(v==null||a==null) throw new BizException("无可用数据");
        MeridianInputDto in=new MeridianInputDto(); in.setHeartRate(v.getHeartRate()); in.setSpo2(v.getSpo2()); try{in.setRrIntervals(om.readValue(v.getRrIntervalsJson(),new TypeReference<List<Integer>>(){}));}catch(Exception e){in.setRrIntervals(List.of());} in.setEmotionState(v.getEmotionState()); in.setDeviceStressScore(v.getDeviceStressScore()); in.setDeviceFatigueScore(v.getDeviceFatigueScore()); in.setDeviceRecoveryScore(v.getDeviceRecoveryScore()); in.setStepCount(a.getStepCount()); in.setCalorieBurn(a.getCalorieBurn()); in.setMeasureTime(v.getMeasureTime());
        MeridianEvaluateRequest req=new MeridianEvaluateRequest(); req.setUserId(userId); req.setInput(in); return evaluateService.evaluate(req);
    }
    public List<TimeslotSummaryVo> getTimeslotSummary(String userId, LocalDate date, String slot){ return summaryMapper.selectTimeslotSummary(userId,date,slot); }
    public DailySummaryVo getDailySummary(String userId, LocalDate date){ return summaryMapper.selectDailySummary(userId,date); }
    public MeridianModelProgress getModelProgress(String userId){ return progressMapper.selectByUserId(userId); }
}
