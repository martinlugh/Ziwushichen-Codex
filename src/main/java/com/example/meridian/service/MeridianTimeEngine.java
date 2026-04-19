package com.example.meridian.service;

import com.example.meridian.common.constants.CommonConstants;
import com.example.meridian.common.exception.BizException;
import com.example.meridian.domain.enums.MeridianTimeSlotEnum;
import com.example.meridian.domain.vo.MeridianWindowVo;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/** 时辰时间引擎 */
@Component
public class MeridianTimeEngine {
    public MeridianTimeSlotEnum resolveCurrentSlot(LocalDateTime measureTime){
        LocalTime t=measureTime.toLocalTime();
        for(MeridianTimeSlotEnum s:MeridianTimeSlotEnum.values()){
            if(containsTime(s,t)) return s;
        }
        throw new BizException("无法匹配时辰");
    }
    public MeridianWindowVo buildWindow(LocalDateTime measureTime,long min){
        if(MeridianTimeSlotEnum.values().length!= CommonConstants.FIXED_MERIDIAN_SLOT_COUNT) throw new BizException("时辰数量异常");
        MeridianTimeSlotEnum slot=resolveCurrentSlot(measureTime);
        LocalDateTime start=resolveSlotStart(measureTime.toLocalDate(),slot,measureTime.toLocalTime());
        LocalDateTime end=resolveSlotEnd(start,slot);
        long cumulative=Duration.between(start,measureTime).toMinutes();
        LocalDateTime ws=cumulative<30?start:measureTime.minusMinutes(30);
        if(ws.isBefore(start)) ws=start;
        LocalDateTime we=measureTime.isAfter(end)?end:measureTime;
        MeridianWindowVo vo=new MeridianWindowVo();
        vo.setMeridianSlotCode(slot.getCode()); vo.setSlotStartTime(start); vo.setSlotEndTime(end); vo.setWindowStartTime(ws); vo.setWindowEndTime(we);
        vo.setCumulativeMinutesFromSlotStart(cumulative); vo.setWindowMinutes(Math.max(0,Duration.between(ws,we).toMinutes()));
        vo.setReachedMinWindow(vo.getWindowMinutes()>=min); vo.setSlotEnded(!measureTime.isBefore(end));
        return vo;
    }
    public MeridianWindowVo buildWindow(LocalDateTime measureTime){ return buildWindow(measureTime,10); }
    private boolean containsTime(MeridianTimeSlotEnum slot,LocalTime t){
        LocalTime s=slot.getStart(),e=slot.getEnd();
        if(!s.isAfter(e)) return !t.isBefore(s)&&!t.isAfter(e);
        return !t.isBefore(s)||!t.isAfter(e);
    }
    private LocalDateTime resolveSlotStart(LocalDate d,MeridianTimeSlotEnum slot,LocalTime t){
        LocalTime s=slot.getStart(),e=slot.getEnd();
        if(!s.isAfter(e)) return LocalDateTime.of(d,s);
        return !t.isAfter(e)?LocalDateTime.of(d.minusDays(1),s):LocalDateTime.of(d,s);
    }
    private LocalDateTime resolveSlotEnd(LocalDateTime start,MeridianTimeSlotEnum slot){
        LocalTime s=slot.getStart(),e=slot.getEnd();
        return !s.isAfter(e)?LocalDateTime.of(start.toLocalDate(),e):LocalDateTime.of(start.toLocalDate().plusDays(1),e);
    }
}
