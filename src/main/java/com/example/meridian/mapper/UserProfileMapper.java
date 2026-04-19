package com.example.meridian.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.meridian.domain.entity.UserProfile;
import org.apache.ibatis.annotations.Mapper;

/** UserProfileMapper */
@Mapper
public interface UserProfileMapper extends BaseMapper<UserProfile> {
}
