package com.ziwushichen.health.domain.request.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 活动数据输入请求。
 */
@Data
public class DeviceActivityRequest {

    @NotBlank(message = "userId不能为空")
    private String userId;

    private Integer stepCount;

    private Double calorieBurn;

    @NotNull(message = "measureTime不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime measureTime;
}
