package com.ziwushichen.health.rule;

import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 时辰规则配置实体。
 */
@Data
public class MeridianRuleConfig {

    private String meridianTimeSlotName;

    private String meridianName;

    private String focusDimension;

    private Map<String, Double> scoreWeight;

    private Map<String, Double> thresholdConfig;

    private List<String> tagRule;

    private String medicalTemplate;

    private String tcmTemplate;

    private String adviceTemplate;
}
