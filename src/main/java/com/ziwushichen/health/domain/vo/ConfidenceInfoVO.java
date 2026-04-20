package com.ziwushichen.health.domain.vo;

import com.ziwushichen.health.enums.ConfidenceLevelEnum;
import lombok.Data;

/**
 * 置信信息展示对象。
 */
@Data
public class ConfidenceInfoVO {

    private Double confidenceScore;

    private ConfidenceLevelEnum confidenceLevel;

    private String confidenceReason;
}
