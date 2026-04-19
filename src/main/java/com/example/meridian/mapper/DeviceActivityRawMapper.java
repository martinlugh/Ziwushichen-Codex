package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.DeviceActivityRaw;
import org.apache.ibatis.annotations.Mapper;

/** DeviceActivityRawMapper */
@Mapper
public interface DeviceActivityRawMapper extends BaseMapper<DeviceActivityRaw> {
    java.util.List<DeviceActivityRaw> selectByUserSlotAndTimeRange(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode,@org.apache.ibatis.annotations.Param("startTime") java.time.LocalDateTime startTime,@org.apache.ibatis.annotations.Param("endTime") java.time.LocalDateTime endTime);
    java.util.List<DeviceActivityRaw> selectRecent3DaysSameSlot(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode,@org.apache.ibatis.annotations.Param("startTime") java.time.LocalDateTime startTime,@org.apache.ibatis.annotations.Param("endTime") java.time.LocalDateTime endTime);
    DeviceActivityRaw selectLatestByUser(@org.apache.ibatis.annotations.Param("userId") String userId);
}
