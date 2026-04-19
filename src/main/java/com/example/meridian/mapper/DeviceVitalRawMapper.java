package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.DeviceVitalRaw;
import org.apache.ibatis.annotations.Mapper;

/** DeviceVitalRawMapper */
@Mapper
public interface DeviceVitalRawMapper extends BaseMapper<DeviceVitalRaw> {
    java.util.List<DeviceVitalRaw> selectByUserSlotAndTimeRange(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode,@org.apache.ibatis.annotations.Param("startTime") java.time.LocalDateTime startTime,@org.apache.ibatis.annotations.Param("endTime") java.time.LocalDateTime endTime);
    java.util.List<DeviceVitalRaw> selectRecent3DaysSameSlot(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode,@org.apache.ibatis.annotations.Param("startTime") java.time.LocalDateTime startTime,@org.apache.ibatis.annotations.Param("endTime") java.time.LocalDateTime endTime);
    DeviceVitalRaw selectLatestByUser(@org.apache.ibatis.annotations.Param("userId") String userId);
    java.util.List<String> selectDistinctUsersRecent(@org.apache.ibatis.annotations.Param("startTime") java.time.LocalDateTime startTime);
}
