package com.example.meridian.domain.vo;
import lombok.Data;import java.math.BigDecimal;
/** 规则阈值 */
@Data public class RuleThresholdVo { private BigDecimal hrLow=new BigDecimal("50"); private BigDecimal hrHigh=new BigDecimal("105"); private BigDecimal spo2Low=new BigDecimal("94"); private BigDecimal stressHigh=new BigDecimal("70"); private BigDecimal fatigueHigh=new BigDecimal("75"); private BigDecimal recoveryLow=new BigDecimal("35"); private BigDecimal criticalHrLow=new BigDecimal("40"); private BigDecimal criticalHrHigh=new BigDecimal("130"); private BigDecimal criticalSpo2Low=new BigDecimal("90"); }
