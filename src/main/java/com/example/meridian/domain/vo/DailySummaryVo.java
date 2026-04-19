package com.example.meridian.domain.vo;
import lombok.Data;import java.math.BigDecimal;import java.time.LocalDate;
/** 每日汇总 */
@Data public class DailySummaryVo { private String userId; private LocalDate summaryDate; private BigDecimal dailyAvgOverallScore; private Integer totalSampleCount; private String dominantColorStatus; }
