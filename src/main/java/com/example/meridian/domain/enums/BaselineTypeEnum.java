package com.example.meridian.domain.enums;
import lombok.Getter;
/** 基线类型 */
@Getter public enum BaselineTypeEnum { GENERAL_REFERENCE("GENERAL_REFERENCE","通用参考"),PERSONAL_HEALTHY("PERSONAL_HEALTHY","个人健康"),PERSONAL_STABLE("PERSONAL_STABLE","个人稳定");
private final String code; private final String desc; BaselineTypeEnum(String c,String d){code=c;desc=d;} }
