package com.ziwushichen.health.store;

import com.ziwushichen.health.domain.entity.BaselineCandidateSegment;
import com.ziwushichen.health.domain.entity.BaselineModelProgress;
import com.ziwushichen.health.domain.entity.MeridianSettlementResult;
import com.ziwushichen.health.domain.entity.MeridianWindowSegment;
import com.ziwushichen.health.domain.entity.RealtimeJudgeResult;
import com.ziwushichen.health.domain.entity.UserBaselineModel;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.HealthColorStatusEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 内存数据仓库。
 * 该仓库替代数据库，存放系统运行期数据。
 */
@Component
public class InMemoryDataStore {

    /**
     * 原始输入数据。
     */
    private final Map<String, CopyOnWriteArrayList<WearableMetricSnapshot>> rawInputDataStore = new ConcurrentHashMap<>();

    /**
     * 时辰窗口片段。
     */
    private final Map<String, MeridianWindowSegment> meridianWindowSegmentStore = new ConcurrentHashMap<>();

    /**
     * 基线候选片段。
     */
    private final Map<String, CopyOnWriteArrayList<BaselineCandidateSegment>> baselineCandidateSegmentStore = new ConcurrentHashMap<>();

    /**
     * 用户基线。
     */
    private final Map<String, UserBaselineModel> userBaselineStore = new ConcurrentHashMap<>();

    /**
     * 建模进度。
     */
    private final Map<String, BaselineModelProgress> baselineModelProgressStore = new ConcurrentHashMap<>();

    /**
     * 实时结果。
     */
    private final Map<String, RealtimeJudgeResult> realtimeResultStore = new ConcurrentHashMap<>();

    /**
     * 实时结果历史。
     */
    private final Map<String, CopyOnWriteArrayList<RealtimeJudgeResult>> realtimeResultHistoryStore = new ConcurrentHashMap<>();

    /**
     * 时辰结算结果。
     */
    private final Map<String, MeridianSettlementResult> meridianSettlementResultStore = new ConcurrentHashMap<>();

    /**
     * 颜色状态历史。
     */
    private final Map<String, CopyOnWriteArrayList<HealthColorStatusEnum>> colorStatusHistoryStore = new ConcurrentHashMap<>();

    /**
     * 最近一次生理输入快照。
     */
    private final Map<String, WearableMetricSnapshot> latestVitalsSnapshotStore = new ConcurrentHashMap<>();

    /**
     * 最近一次活动输入快照。
     */
    private final Map<String, WearableMetricSnapshot> latestActivitySnapshotStore = new ConcurrentHashMap<>();

    public void addRawInputData(WearableMetricSnapshot snapshot) {
        String key = buildUserDateKey(snapshot.getUserId(), snapshot.getMeasureTime().toLocalDate());
        rawInputDataStore.computeIfAbsent(key, ignored -> new CopyOnWriteArrayList<>()).add(snapshot);
    }

    public List<WearableMetricSnapshot> getRawInputData(String userId, LocalDate localDate) {
        String key = buildUserDateKey(userId, localDate);
        return rawInputDataStore.getOrDefault(key, new CopyOnWriteArrayList<>());
    }

    public void saveMeridianWindowSegment(String userId,
                                          MeridianTimeSlotEnum meridianTimeSlot,
                                          MeridianWindowSegment meridianWindowSegment) {
        meridianWindowSegmentStore.put(buildUserSlotKey(userId, meridianTimeSlot), meridianWindowSegment);
    }

    public MeridianWindowSegment getMeridianWindowSegment(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        return meridianWindowSegmentStore.get(buildUserSlotKey(userId, meridianTimeSlot));
    }

    public void addBaselineCandidateSegment(String userId, BaselineCandidateSegment baselineCandidateSegment) {
        baselineCandidateSegmentStore.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(baselineCandidateSegment);
    }

    public List<BaselineCandidateSegment> getBaselineCandidateSegmentList(String userId) {
        return baselineCandidateSegmentStore.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }

    public void saveUserBaseline(String userId, MeridianTimeSlotEnum meridianTimeSlot, UserBaselineModel userBaselineModel) {
        userBaselineStore.put(buildUserSlotKey(userId, meridianTimeSlot), userBaselineModel);
    }

    public UserBaselineModel getUserBaseline(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        return userBaselineStore.get(buildUserSlotKey(userId, meridianTimeSlot));
    }

    public void saveBaselineModelProgress(String userId,
                                          MeridianTimeSlotEnum meridianTimeSlot,
                                          BaselineModelProgress baselineModelProgress) {
        baselineModelProgressStore.put(buildUserSlotKey(userId, meridianTimeSlot), baselineModelProgress);
    }

    public BaselineModelProgress getBaselineModelProgress(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        return baselineModelProgressStore.get(buildUserSlotKey(userId, meridianTimeSlot));
    }

    public void saveRealtimeResult(String userId, RealtimeJudgeResult realtimeJudgeResult) {
        realtimeResultStore.put(userId, realtimeJudgeResult);
        realtimeResultHistoryStore.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(realtimeJudgeResult);
    }

    public RealtimeJudgeResult getRealtimeResult(String userId) {
        return realtimeResultStore.get(userId);
    }

    public List<RealtimeJudgeResult> getRealtimeResultHistory(String userId) {
        return realtimeResultHistoryStore.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }

    public void saveMeridianSettlementResult(String userId,
                                             MeridianTimeSlotEnum meridianTimeSlot,
                                             MeridianSettlementResult meridianSettlementResult) {
        meridianSettlementResultStore.put(buildUserSlotKey(userId, meridianTimeSlot), meridianSettlementResult);
    }

    public MeridianSettlementResult getMeridianSettlementResult(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        return meridianSettlementResultStore.get(buildUserSlotKey(userId, meridianTimeSlot));
    }

    public void addColorStatusHistory(String userId, HealthColorStatusEnum healthColorStatus) {
        colorStatusHistoryStore.computeIfAbsent(userId, ignored -> new CopyOnWriteArrayList<>()).add(healthColorStatus);
    }

    public List<HealthColorStatusEnum> getColorStatusHistory(String userId) {
        return colorStatusHistoryStore.getOrDefault(userId, new CopyOnWriteArrayList<>());
    }

    public void saveLatestVitalsSnapshot(String userId, WearableMetricSnapshot snapshot) {
        latestVitalsSnapshotStore.put(userId, snapshot);
    }

    public WearableMetricSnapshot getLatestVitalsSnapshot(String userId) {
        return latestVitalsSnapshotStore.get(userId);
    }

    public void saveLatestActivitySnapshot(String userId, WearableMetricSnapshot snapshot) {
        latestActivitySnapshotStore.put(userId, snapshot);
    }

    public WearableMetricSnapshot getLatestActivitySnapshot(String userId) {
        return latestActivitySnapshotStore.get(userId);
    }

    public Set<String> listUserIdSet() {
        Set<String> userIdSet = new HashSet<>();
        for (String key : rawInputDataStore.keySet()) {
            String[] partList = key.split(":");
            if (partList.length >= 1) {
                userIdSet.add(partList[0]);
            }
        }
        userIdSet.addAll(latestVitalsSnapshotStore.keySet());
        userIdSet.addAll(latestActivitySnapshotStore.keySet());
        return userIdSet;
    }

    private String buildUserDateKey(String userId, LocalDate localDate) {
        return userId + ":" + localDate;
    }

    private String buildUserSlotKey(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        return userId + ":" + meridianTimeSlot.name();
    }
}
