package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.dto.BaselineHealthFilterResultDTO;
import com.ziwushichen.health.domain.entity.BaselineCandidateSegment;
import com.ziwushichen.health.domain.entity.BaselineModelProgress;
import com.ziwushichen.health.domain.entity.UserBaselineModel;
import com.ziwushichen.health.domain.entity.UserBaselineProfile;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.ActivitySceneTypeEnum;
import com.ziwushichen.health.enums.BaselineEligibilityLevelEnum;
import com.ziwushichen.health.enums.BaselineModelTypeEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.store.InMemoryDataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 时辰基线构建服务。
 */
@Service
public class MeridianBaselineBuildService {

    /**
     * 健康候选最小数量。
     */
    private static final int MIN_HEALTHY_COUNT = 6;

    /**
     * 稳定候选最小数量。
     */
    private static final int MIN_STABLE_COUNT = 4;

    private final InMemoryDataStore inMemoryDataStore;
    private final BaselineHealthFilterService baselineHealthFilterService;
    private final ActivitySceneService activitySceneService;

    public MeridianBaselineBuildService(InMemoryDataStore inMemoryDataStore,
                                        BaselineHealthFilterService baselineHealthFilterService,
                                        ActivitySceneService activitySceneService) {
        this.inMemoryDataStore = inMemoryDataStore;
        this.baselineHealthFilterService = baselineHealthFilterService;
        this.activitySceneService = activitySceneService;
    }

    /**
     * 构建用户指定时辰的基线。
     *
     * @param userId 用户标识
     * @param meridianTimeSlot 时辰
     * @param currentTime 当前时间
     * @return 基线实体
     */
    public UserBaselineModel buildBaseline(String userId,
                                           MeridianTimeSlotEnum meridianTimeSlot,
                                           LocalDateTime currentTime) {
        List<WearableMetricSnapshot> recentThreeDaySnapshotList = loadRecentThreeDaySnapshotList(userId, currentTime)
                .stream()
                .filter(item -> item.getMeridianTimeSlot() == meridianTimeSlot)
                .sorted(Comparator.comparing(WearableMetricSnapshot::getMeasureTime))
                .collect(Collectors.toList());

        List<BaselineCandidateSegment> candidateSegmentList = new ArrayList<>();
        for (WearableMetricSnapshot snapshot : recentThreeDaySnapshotList) {
            ActivitySceneTypeEnum activityScene = activitySceneService.identifyActivityScene(snapshot);
            BaselineHealthFilterResultDTO filterResultDTO = baselineHealthFilterService.filter(snapshot, activityScene);

            BaselineCandidateSegment candidateSegment = new BaselineCandidateSegment();
            candidateSegment.setUserId(userId);
            candidateSegment.setMeridianTimeSlot(meridianTimeSlot);
            candidateSegment.setSegmentStartTime(snapshot.getMeasureTime());
            candidateSegment.setSegmentEndTime(snapshot.getMeasureTime());
            candidateSegment.setMetricSnapshotList(List.of(snapshot));
            candidateSegment.setActivityScene(activityScene);
            candidateSegment.setBaselineEligibilityLevel(filterResultDTO.getBaselineEligibilityLevel());
            candidateSegment.setBaselineHealthScore(filterResultDTO.getBaselineHealthScore());
            candidateSegment.setBaselineRejectReasons(filterResultDTO.getBaselineRejectReasons());
            candidateSegment.setBaselineTypeUsed(filterResultDTO.getBaselineTypeUsed());
            candidateSegment.setConfidenceLevel(filterResultDTO.getConfidenceLevel());
            candidateSegmentList.add(candidateSegment);
            inMemoryDataStore.addBaselineCandidateSegment(userId, candidateSegment);
        }

        List<WearableMetricSnapshot> healthySnapshotList = candidateSegmentList.stream()
                .filter(item -> item.getBaselineEligibilityLevel() == BaselineEligibilityLevelEnum.FULLY_ELIGIBLE)
                .map(item -> item.getMetricSnapshotList().get(0))
                .collect(Collectors.toList());

        List<WearableMetricSnapshot> stableSnapshotList = candidateSegmentList.stream()
                .filter(item -> item.getBaselineEligibilityLevel() == BaselineEligibilityLevelEnum.FULLY_ELIGIBLE
                        || item.getBaselineEligibilityLevel() == BaselineEligibilityLevelEnum.PARTIALLY_ELIGIBLE)
                .map(item -> item.getMetricSnapshotList().get(0))
                .collect(Collectors.toList());

        UserBaselineModel userBaselineModel = new UserBaselineModel();
        userBaselineModel.setUserId(userId);
        userBaselineModel.setMeridianTimeSlot(meridianTimeSlot);
        userBaselineModel.setBaselineBuildTime(currentTime);

        if (healthySnapshotList.size() >= MIN_HEALTHY_COUNT) {
            userBaselineModel.setBaselineType(BaselineModelTypeEnum.PERSONAL_HEALTHY);
            userBaselineModel.setBaselineMetricSnapshot(aggregateBaseline(healthySnapshotList));
        } else if (stableSnapshotList.size() >= MIN_STABLE_COUNT) {
            userBaselineModel.setBaselineType(BaselineModelTypeEnum.PERSONAL_STABLE);
            userBaselineModel.setBaselineMetricSnapshot(aggregateBaseline(stableSnapshotList));
        } else {
            userBaselineModel.setBaselineType(BaselineModelTypeEnum.GENERAL_REFERENCE);
            userBaselineModel.setBaselineMetricSnapshot(aggregateBaseline(recentThreeDaySnapshotList));
        }

        inMemoryDataStore.saveUserBaseline(userId, meridianTimeSlot, userBaselineModel);
        saveModelProgress(userId, meridianTimeSlot, currentTime, candidateSegmentList, userBaselineModel.getBaselineType());
        return userBaselineModel;
    }

    private List<WearableMetricSnapshot> loadRecentThreeDaySnapshotList(String userId, LocalDateTime currentTime) {
        List<WearableMetricSnapshot> snapshotList = new ArrayList<>();
        for (int dayOffset = 0; dayOffset < 3; dayOffset++) {
            LocalDate date = currentTime.toLocalDate().minusDays(dayOffset);
            snapshotList.addAll(inMemoryDataStore.getRawInputData(userId, date));
        }
        return snapshotList;
    }

    private UserBaselineProfile.BaselineMetricSnapshot aggregateBaseline(List<WearableMetricSnapshot> snapshotList) {
        UserBaselineProfile.BaselineMetricSnapshot baselineMetricSnapshot = new UserBaselineProfile.BaselineMetricSnapshot();
        if (snapshotList == null || snapshotList.isEmpty()) {
            return baselineMetricSnapshot;
        }

        baselineMetricSnapshot.setHeartRate(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getHeartRate).collect(Collectors.toList())));
        baselineMetricSnapshot.setSpo2(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSpo2).collect(Collectors.toList())));
        baselineMetricSnapshot.setSd1(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSd1).collect(Collectors.toList())));
        baselineMetricSnapshot.setSd2(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSd2).collect(Collectors.toList())));
        baselineMetricSnapshot.setHf(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getHf).collect(Collectors.toList())));
        baselineMetricSnapshot.setLf(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getLf).collect(Collectors.toList())));
        baselineMetricSnapshot.setVlf(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getVlf).collect(Collectors.toList())));
        baselineMetricSnapshot.setSampleEntropy(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSampleEntropy).collect(Collectors.toList())));
        baselineMetricSnapshot.setApproximateEntropy(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getApproximateEntropy).collect(Collectors.toList())));
        baselineMetricSnapshot.setDfaAlpha1(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getDfaAlpha1).collect(Collectors.toList())));
        baselineMetricSnapshot.setDfaAlpha2(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getDfaAlpha2).collect(Collectors.toList())));
        baselineMetricSnapshot.setRespiratoryRate(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getRespiratoryRate).collect(Collectors.toList())));
        baselineMetricSnapshot.setStepCount(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getStepCount).collect(Collectors.toList())));
        baselineMetricSnapshot.setCalorieBurn(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getCalorieBurn).collect(Collectors.toList())));
        baselineMetricSnapshot.setDeviceStressScore(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getDeviceStressScore).collect(Collectors.toList())));
        baselineMetricSnapshot.setDeviceFatigueScore(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getDeviceFatigueScore).collect(Collectors.toList())));
        baselineMetricSnapshot.setDeviceRecoveryScore(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getDeviceRecoveryScore).collect(Collectors.toList())));
        return baselineMetricSnapshot;
    }

    private void saveModelProgress(String userId,
                                   MeridianTimeSlotEnum meridianTimeSlot,
                                   LocalDateTime currentTime,
                                   List<BaselineCandidateSegment> candidateSegmentList,
                                   BaselineModelTypeEnum baselineModelType) {
        BaselineModelProgress modelProgress = new BaselineModelProgress();
        modelProgress.setUserId(userId);
        modelProgress.setMeridianTimeSlot(meridianTimeSlot);
        modelProgress.setCandidateCount(candidateSegmentList.size());
        modelProgress.setEligibleCount((int) candidateSegmentList.stream()
                .filter(item -> item.getBaselineEligibilityLevel() != BaselineEligibilityLevelEnum.NOT_ELIGIBLE)
                .count());
        modelProgress.setCurrentType(baselineModelType);
        modelProgress.setProgressRemark("按近3天健康筛选结果构建完成");
        modelProgress.setUpdateTime(currentTime);
        inMemoryDataStore.saveBaselineModelProgress(userId, meridianTimeSlot, modelProgress);
    }

    private Double avgDouble(List<Double> valueList) {
        List<Double> validValueList = valueList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (validValueList.isEmpty()) {
            return null;
        }
        return validValueList.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
    }

    private Integer avgInteger(List<Integer> valueList) {
        List<Integer> validValueList = valueList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (validValueList.isEmpty()) {
            return null;
        }
        return (int) Math.round(validValueList.stream().mapToInt(Integer::intValue).average().orElse(0D));
    }
}
