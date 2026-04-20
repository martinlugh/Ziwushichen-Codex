package com.ziwushichen.health.common.util;

import com.ziwushichen.health.domain.entity.UserBaselineProfile;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存存储工具类。
 * 本系统仅使用内存结构存放运行期数据，不引入数据库。
 */
public final class MemoryStoreUtil {

    private MemoryStoreUtil() {
    }

    /**
     * 用户指标快照内存仓库。
     */
    private static final Map<String, WearableMetricSnapshot> USER_METRIC_SNAPSHOT_STORE = new ConcurrentHashMap<>();

    /**
     * 用户基线画像内存仓库。
     */
    private static final Map<String, UserBaselineProfile> USER_BASELINE_PROFILE_STORE = new ConcurrentHashMap<>();

    public static Map<String, WearableMetricSnapshot> getUserMetricSnapshotStore() {
        return USER_METRIC_SNAPSHOT_STORE;
    }

    public static Map<String, UserBaselineProfile> getUserBaselineProfileStore() {
        return USER_BASELINE_PROFILE_STORE;
    }
}
