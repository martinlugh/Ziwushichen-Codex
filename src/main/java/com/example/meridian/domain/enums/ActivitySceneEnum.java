package com.example.meridian.domain.enums;
import lombok.Getter;
/** 活动场景 */
@Getter public enum ActivitySceneEnum { REST("REST","静息"),LIGHT_ACTIVITY("LIGHT_ACTIVITY","轻活动"),ACTIVE("ACTIVE","活跃"),POST_ACTIVITY_RECOVERY("POST_ACTIVITY_RECOVERY","活动后恢复");
private final String code; private final String desc; ActivitySceneEnum(String c,String d){code=c;desc=d;} }
