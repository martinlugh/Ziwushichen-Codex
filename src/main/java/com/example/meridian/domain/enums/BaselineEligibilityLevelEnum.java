package com.example.meridian.domain.enums;
import lombok.Getter;
/** 基线资格 */
@Getter public enum BaselineEligibilityLevelEnum { REJECT("REJECT","拒绝"),WEAK("WEAK","较弱"),PASS("PASS","通过"),STRONG("STRONG","强");
private final String code; private final String desc; BaselineEligibilityLevelEnum(String c,String d){code=c;desc=d;} }
