package com.ziwushichen.health.common.constant;

/**
 * 缓存键常量。
 */
public final class CacheConstant {

    private CacheConstant() {
    }

    /**
     * 用户指标快照缓存键前缀。
     */
    public static final String USER_METRIC_SNAPSHOT_PREFIX = "user_metric_snapshot:";

    /**
     * 用户时辰基线缓存键前缀。
     */
    public static final String USER_BASELINE_PROFILE_PREFIX = "user_baseline_profile:";
}
