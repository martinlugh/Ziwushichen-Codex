package com.ziwushichen.health.domain.response.api;

import lombok.Data;

import java.util.List;

/**
 * 每日总结响应。
 */
@Data
public class DailySummaryResponse {

    private String userId;

    private String date;

    private Integer sampleCount;

    private Double avgOverallScore;

    private String dailyConclusion;

    private List<String> keyObservations;
}
