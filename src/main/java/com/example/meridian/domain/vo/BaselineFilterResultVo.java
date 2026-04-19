package com.example.meridian.domain.vo;
import lombok.Data;import java.math.BigDecimal;import java.util.*;
/** 基线筛选结果 */
@Data public class BaselineFilterResultVo { private String baselineEligibilityLevel; private BigDecimal baselineHealthScore; private List<String> baselineRejectReasons=new ArrayList<>(); private String baselineTypeUsed; private String confidenceLevel; }
