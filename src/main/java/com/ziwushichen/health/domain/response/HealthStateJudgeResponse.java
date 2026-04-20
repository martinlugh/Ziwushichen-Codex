package com.ziwushichen.health.domain.response;

import lombok.Data;

import java.util.List;

/**
 * 健康状态判断响应对象。
 */
@Data
public class HealthStateJudgeResponse {

    private Double overallScore;
    private Double loadScore;
    private Double recoveryHealthScore;
    private Double oxygenScore;
    private Double rhythmMatchScore;
    private Double baselineDeviationScore;
    private Double confidenceScore;
    private String healthColorStatus;
    private List<String> mainDrivers;
    private String colorChangeReason;
    private String medicalSummary;
    private String tcmMeridianSummary;
    private List<String> actionAdvice;
}
