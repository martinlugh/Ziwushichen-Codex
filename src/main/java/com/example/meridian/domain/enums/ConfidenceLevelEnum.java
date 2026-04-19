package com.example.meridian.domain.enums;
import lombok.Getter;
/** 置信等级 */
@Getter public enum ConfidenceLevelEnum { LOW("LOW","低"),MEDIUM("MEDIUM","中"),HIGH("HIGH","高");
private final String code; private final String desc; ConfidenceLevelEnum(String c,String d){code=c;desc=d;} }
