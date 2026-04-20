package com.ziwushichen.health.domain.dto;

import com.ziwushichen.health.enums.BaselineEligibilityLevelEnum;
import com.ziwushichen.health.enums.BaselineModelTypeEnum;
import com.ziwushichen.health.enums.ConfidenceLevelEnum;
import lombok.Data;

import java.util.List;

/**
 * 基线健康筛选结果。
 */
@Data
public class BaselineHealthFilterResultDTO {

    private BaselineEligibilityLevelEnum baselineEligibilityLevel;

    private Double baselineHealthScore;

    private List<String> baselineRejectReasons;

    private BaselineModelTypeEnum baselineTypeUsed;

    private ConfidenceLevelEnum confidenceLevel;
}
