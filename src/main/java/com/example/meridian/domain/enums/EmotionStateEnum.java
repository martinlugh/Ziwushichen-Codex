package com.example.meridian.domain.enums;
import lombok.Getter;
/** 情绪状态 */
@Getter public enum EmotionStateEnum { CALM("CALM","平静"),STRESSED("STRESSED","紧张"),ANXIOUS("ANXIOUS","焦虑"),TIRED("TIRED","疲惫"),EXCITED("EXCITED","兴奋");
private final String code; private final String desc; EmotionStateEnum(String c,String d){code=c;desc=d;} }
