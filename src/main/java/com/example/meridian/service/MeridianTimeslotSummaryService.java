package com.example.meridian.service;

import com.example.meridian.domain.entity.MeridianRealtimeResult;
import com.example.meridian.domain.vo.MeridianWindowVo;
import com.example.meridian.mapper.MeridianRealtimeResultMapper;
import com.example.meridian.mapper.MeridianTimeslotSummaryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** 时辰结算服务 */
@Service
@RequiredArgsConstructor
public class MeridianTimeslotSummaryService {
    private final MeridianTimeEngine timeEngine; private final MeridianRealtimeResultMapper resultMapper; private final MeridianTimeslotSummaryMapper summaryMapper;
    @Transactional(rollbackFor = Exception.class)
    public void settleIfSlotEnded(String userId, LocalDateTime measureTime){
        MeridianWindowVo w=timeEngine.buildWindow(measureTime,1); if(!w.isSlotEnded()) return;
        List<MeridianRealtimeResult> list=resultMapper.selectByUserSlotAndTimeRange(userId,w.getMeridianSlotCode(),w.getSlotStartTime(),w.getSlotEndTime());
        if(list==null||list.isEmpty()) return;
        BigDecimal avg=list.stream().map(MeridianRealtimeResult::getOverallScore).reduce(BigDecimal.ZERO,BigDecimal::add).divide(BigDecimal.valueOf(list.size()),2, RoundingMode.HALF_UP);
        String color=list.stream().collect(Collectors.groupingBy(MeridianRealtimeResult::getHealthColorStatus,Collectors.counting())).entrySet().stream().max(Comparator.comparingLong(Map.Entry::getValue)).map(Map.Entry::getKey).orElse("DATA_INSUFFICIENT");
        summaryMapper.upsertSummary(userId,w.getSlotStartTime().toLocalDate().toString(),w.getMeridianSlotCode(),avg,color,list.size());
    }
}
