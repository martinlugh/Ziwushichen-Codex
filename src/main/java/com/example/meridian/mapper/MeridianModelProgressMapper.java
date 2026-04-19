package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.MeridianModelProgress;
import org.apache.ibatis.annotations.Mapper;

/** MeridianModelProgressMapper */
@Mapper
public interface MeridianModelProgressMapper extends BaseMapper<MeridianModelProgress> {
    int upsertProgress(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("completedSlots") Integer completedSlots,@org.apache.ibatis.annotations.Param("totalSlots") Integer totalSlots,@org.apache.ibatis.annotations.Param("progressPercent") java.math.BigDecimal progressPercent,@org.apache.ibatis.annotations.Param("progressNote") String progressNote);
    MeridianModelProgress selectByUserId(@org.apache.ibatis.annotations.Param("userId") String userId);
}
