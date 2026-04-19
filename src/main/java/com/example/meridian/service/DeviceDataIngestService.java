package com.example.meridian.service;

import com.example.meridian.domain.entity.DeviceActivityRaw;
import com.example.meridian.domain.entity.DeviceVitalRaw;
import com.example.meridian.domain.request.ActivityUploadRequest;
import com.example.meridian.domain.request.VitalUploadRequest;
import com.example.meridian.domain.vo.MeridianWindowVo;
import com.example.meridian.mapper.DeviceActivityRawMapper;
import com.example.meridian.mapper.DeviceVitalRawMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 数据入库服务 */
@Service
@RequiredArgsConstructor
public class DeviceDataIngestService {
    private final MeridianTimeEngine timeEngine; private final DeviceVitalRawMapper vitalMapper; private final DeviceActivityRawMapper activityMapper; private final ObjectMapper om=new ObjectMapper();
    @Transactional(rollbackFor = Exception.class)
    public void saveVitals(VitalUploadRequest r){
        MeridianWindowVo w=timeEngine.buildWindow(r.getMeasureTime(),1);
        DeviceVitalRaw row=new DeviceVitalRaw(); row.setUserId(r.getUserId()); row.setHeartRate(r.getHeartRate()); row.setSpo2(r.getSpo2()); try{row.setRrIntervalsJson(om.writeValueAsString(r.getRrIntervals()));}catch(Exception e){row.setRrIntervalsJson("[]");} row.setEmotionState(r.getEmotionState()); row.setDeviceStressScore(r.getDeviceStressScore()); row.setDeviceFatigueScore(r.getDeviceFatigueScore()); row.setDeviceRecoveryScore(r.getDeviceRecoveryScore()); row.setMeasureTime(r.getMeasureTime()); row.setMeridianSlotCode(w.getMeridianSlotCode()); row.setCreatedAt(LocalDateTime.now()); vitalMapper.insert(row);
    }
    @Transactional(rollbackFor = Exception.class)
    public void saveActivity(ActivityUploadRequest r){
        MeridianWindowVo w=timeEngine.buildWindow(r.getMeasureTime(),1);
        DeviceActivityRaw row=new DeviceActivityRaw(); row.setUserId(r.getUserId()); row.setStepCount(r.getStepCount()); row.setCalorieBurn(r.getCalorieBurn()); row.setActivityScene(r.getActivityScene()); row.setMeasureTime(r.getMeasureTime()); row.setMeridianSlotCode(w.getMeridianSlotCode()); row.setCreatedAt(LocalDateTime.now()); activityMapper.insert(row);
    }
}
