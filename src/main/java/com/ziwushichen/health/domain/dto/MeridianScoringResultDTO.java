package com.ziwushichen.health.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 评分结果对象。
 */
@Data
public class MeridianScoringResultDTO {

    private Double overallScore;
    private Double loadScore;
    private Double recoveryHealthScore;
    private Double oxygenScore;
    private Double rhythmMatchScore;
    private Double baselineDeviationScore;
    private Double confidenceScore;

    private List<String> mainDrivers;
}
