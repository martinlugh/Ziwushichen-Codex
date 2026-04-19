package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.UserMeridianBaseline;
import org.apache.ibatis.annotations.Mapper;

/** UserMeridianBaselineMapper */
@Mapper
public interface UserMeridianBaselineMapper extends BaseMapper<UserMeridianBaseline> {
    int upsertBaseline(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("baselineType") String baselineType,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode,@org.apache.ibatis.annotations.Param("baselineHr") java.math.BigDecimal baselineHr,@org.apache.ibatis.annotations.Param("baselineSpo2") java.math.BigDecimal baselineSpo2,@org.apache.ibatis.annotations.Param("baselineStress") java.math.BigDecimal baselineStress,@org.apache.ibatis.annotations.Param("baselineFatigue") java.math.BigDecimal baselineFatigue,@org.apache.ibatis.annotations.Param("baselineRecovery") java.math.BigDecimal baselineRecovery,@org.apache.ibatis.annotations.Param("confidenceLevel") String confidenceLevel,@org.apache.ibatis.annotations.Param("confidenceScore") java.math.BigDecimal confidenceScore);
    UserMeridianBaseline selectByUserTypeAndSlot(@org.apache.ibatis.annotations.Param("userId") String userId,@org.apache.ibatis.annotations.Param("baselineType") String baselineType,@org.apache.ibatis.annotations.Param("meridianSlotCode") String meridianSlotCode);
}
