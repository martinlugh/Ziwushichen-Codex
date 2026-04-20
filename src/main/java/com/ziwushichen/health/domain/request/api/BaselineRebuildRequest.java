package com.ziwushichen.health.domain.request.api;

import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 基线重建请求。
 */
@Data
public class BaselineRebuildRequest {

    @NotBlank(message = "userId不能为空")
    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;
}
