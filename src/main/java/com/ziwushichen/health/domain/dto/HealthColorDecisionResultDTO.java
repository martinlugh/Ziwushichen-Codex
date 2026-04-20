package com.ziwushichen.health.domain.dto;

import com.ziwushichen.health.enums.HealthColorStatusEnum;
import lombok.Data;

import java.util.List;

/**
 * 颜色决策结果对象。
 */
@Data
public class HealthColorDecisionResultDTO {

    private HealthColorStatusEnum healthColorStatus;

    private List<String> mainDrivers;

    private String colorChangeReason;
}
