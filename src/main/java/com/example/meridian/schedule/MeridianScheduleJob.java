package com.example.meridian.schedule;

import com.example.meridian.domain.dto.MeridianInputDto;
import com.example.meridian.domain.entity.DeviceActivityRaw;
import com.example.meridian.domain.entity.DeviceVitalRaw;
import com.example.meridian.domain.enums.MeridianTimeSlotEnum;
import com.example.meridian.domain.request.MeridianEvaluateRequest;
import com.example.meridian.mapper.DeviceActivityRawMapper;
import com.example.meridian.mapper.DeviceVitalRawMapper;
import com.example.meridian.service.MeridianBaselineBuildService;
import com.example.meridian.service.MeridianRealtimeEvaluateService;
import com.example.meridian.service.MeridianTimeslotSummaryService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/** 定时任务 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MeridianScheduleJob {
    private final DeviceVitalRawMapper vitalMapper; private final DeviceActivityRawMapper activityMapper; private final MeridianBaselineBuildService baselineService; private final MeridianRealtimeEvaluateService evaluateService; private final MeridianTimeslotSummaryService summaryService; private final ObjectMapper om=new ObjectMapper();
    @Scheduled(cron = "0 5 * * * ?")
    public void baselineBuildJob(){
        List<String> users=vitalMapper.selectDistinctUsersRecent(LocalDateTime.now().minusDays(1));
        for(String u:users){ for(MeridianTimeSlotEnum s:MeridianTimeSlotEnum.values()){ try{ baselineService.build(u,s.getCode(),LocalDateTime.now()); }catch(Exception e){ log.warn("基线构建失败 user={} slot={}",u,s.getCode()); } } }
    }
    @Scheduled(cron = "0 */5 * * * ?")
    public void realtimeEvaluateJob(){
        List<String> users=vitalMapper.selectDistinctUsersRecent(LocalDateTime.now().minusDays(1));
        for(String u:users){
            DeviceVitalRaw v=vitalMapper.selectLatestByUser(u); DeviceActivityRaw a=activityMapper.selectLatestByUser(u); if(v==null||a==null) continue;
            MeridianInputDto in=new MeridianInputDto(); in.setHeartRate(v.getHeartRate()); in.setSpo2(v.getSpo2()); try{in.setRrIntervals(om.readValue(v.getRrIntervalsJson(),new TypeReference<List<Integer>>(){}));}catch(Exception e){in.setRrIntervals(List.of());} in.setEmotionState(v.getEmotionState()); in.setDeviceStressScore(v.getDeviceStressScore()); in.setDeviceFatigueScore(v.getDeviceFatigueScore()); in.setDeviceRecoveryScore(v.getDeviceRecoveryScore()); in.setStepCount(a.getStepCount()); in.setCalorieBurn(a.getCalorieBurn()); in.setMeasureTime(v.getMeasureTime());
            MeridianEvaluateRequest r=new MeridianEvaluateRequest(); r.setUserId(u); r.setInput(in);
            try{ evaluateService.evaluate(r); }catch(Exception e){ log.warn("实时计算失败 user={}",u); }
        }
    }
    @Scheduled(cron = "0 */10 * * * ?")
    public void timeslotSettleJob(){
        List<String> users=vitalMapper.selectDistinctUsersRecent(LocalDateTime.now().minusDays(1));
        for(String u:users){ try{ summaryService.settleIfSlotEnded(u,LocalDateTime.now()); }catch(Exception e){ log.warn("时辰结算失败 user={}",u); } }
    }
}
