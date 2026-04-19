package com.example.meridian.domain.enums;
import lombok.Getter;
/** 颜色状态固定枚举 */
@Getter public enum HealthColorStatusEnum { DATA_INSUFFICIENT("DATA_INSUFFICIENT","数据不足"),HEALTHY("HEALTHY","健康"),SUB_HEALTH("SUB_HEALTH","亚健康"),IMBALANCE_ALERT("IMBALANCE_ALERT","失衡预警，不能用于疾病诊断");
private final String code; private final String desc; HealthColorStatusEnum(String c,String d){code=c;desc=d;} }
