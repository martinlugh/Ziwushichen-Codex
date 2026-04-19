package com.example.meridian.domain.response;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
/** 评估响应 */
@Data
public class MeridianEvaluateResponse {
    private BigDecimal overallScore; private BigDecimal loadScore; private BigDecimal recoveryHealthScore; private BigDecimal oxygenScore; private BigDecimal rhythmMatchScore; private BigDecimal baselineDeviationScore; private BigDecimal confidenceScore;
    private String healthColorStatus; private List<String> mainDrivers; private String colorChangeReason; private String medicalSummary; private String tcmMeridianSummary; private List<String> actionAdvice;
}
