package com.example.meridian.domain.vo;
import lombok.Data;import java.math.BigDecimal;import java.util.*;
/** 评分结果 */
@Data public class ScoringResultVo { private BigDecimal overallScore; private BigDecimal loadScore; private BigDecimal recoveryHealthScore; private BigDecimal oxygenScore; private BigDecimal rhythmMatchScore; private BigDecimal baselineDeviationScore; private BigDecimal confidenceScore; private List<String> mainDrivers=new ArrayList<>(); }
