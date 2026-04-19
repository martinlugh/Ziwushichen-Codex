package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.MeridianRealtimeResult;
import org.apache.ibatis.annotations.Mapper;

/** MeridianRealtimeResultMapper */
@Mapper
public interface MeridianRealtimeResultMapper extends BaseMapper<MeridianRealtimeResult> {
    MeridianRealtimeResult selectLatestByUser(@org.apache.ibatis.annotations.Param("userId") String userId);
    java.util.List<MeridianRealtimeResult> selectByUserSlotAndTimeRange(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode,@org.apache.ibatis.annotations.Param("startTime") java.time.LocalDateTime startTime,@org.apache.ibatis.annotations.Param("endTime") java.time.LocalDateTime endTime);
    Integer countRecentRed(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("minutes") Integer minutes);
}
