package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.MeridianTimeslotSummary;
import org.apache.ibatis.annotations.Mapper;

/** MeridianTimeslotSummaryMapper */
@Mapper
public interface MeridianTimeslotSummaryMapper extends BaseMapper<MeridianTimeslotSummary> {
    int upsertSummary(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("summaryDate") String summaryDate,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode,@org.apache.ibatis.annotations.Param("avgOverallScore") java.math.BigDecimal avgOverallScore,@org.apache.ibatis.annotations.Param("dominantColorStatus") String dominantColorStatus,@org.apache.ibatis.annotations.Param("sampleCount") Integer sampleCount);
    java.util.List<com.example.meridian.domain.vo.TimeslotSummaryVo> selectTimeslotSummary(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("summaryDate") java.time.LocalDate summaryDate,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode);
    com.example.meridian.domain.vo.DailySummaryVo selectDailySummary(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("summaryDate") java.time.LocalDate summaryDate);
}
