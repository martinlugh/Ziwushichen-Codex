package com.example.meridian.domain.vo;
import lombok.Data;import java.math.BigDecimal;import java.time.LocalDate;
/** 时辰汇总 */
@Data public class TimeslotSummaryVo { private String userId; private LocalDate summaryDate; private String meridianSlotCode; private BigDecimal avgOverallScore; private String dominantColorStatus; private Integer sampleCount; }
