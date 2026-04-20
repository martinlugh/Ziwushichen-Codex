package com.ziwushichen.health.service;

import com.ziwushichen.health.common.exception.BizException;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.domain.request.HealthStateJudgeRequest;
import com.ziwushichen.health.domain.request.api.DeviceActivityRequest;
import com.ziwushichen.health.domain.request.api.DeviceVitalsRequest;
import com.ziwushichen.health.enums.EmotionStateEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.store.InMemoryDataStore;
import org.springframework.stereotype.Service;

/**
 * 设备数据接入服务。
 */
@Service
public class DeviceDataIngestService {

    private final InMemoryDataStore inMemoryDataStore;
    private final MeridianTimeEngine meridianTimeEngine;

    public DeviceDataIngestService(InMemoryDataStore inMemoryDataStore,
                                   MeridianTimeEngine meridianTimeEngine) {
        this.inMemoryDataStore = inMemoryDataStore;
        this.meridianTimeEngine = meridianTimeEngine;
    }

    /**
     * 接收生理数据。
     *
     * @param request 生理数据请求
     */
    public void ingestVitals(DeviceVitalsRequest request) {
        if (request.getEmotionState() != null && !EmotionStateEnum.isValidInput(request.getEmotionState())) {
            throw new BizException("EMOTION_STATE_INVALID", "emotionState仅支持：愉悦、平静、不愉悦");
        }

        WearableMetricSnapshot snapshot = new WearableMetricSnapshot();
        snapshot.setUserId(request.getUserId());
        snapshot.setHeartRate(request.getHeartRate());
        snapshot.setSpo2(request.getSpo2());
        snapshot.setSd1(request.getSd1());
        snapshot.setSd2(request.getSd2());
        snapshot.setHf(request.getHf());
        snapshot.setLf(request.getLf());
        snapshot.setVlf(request.getVlf());
        snapshot.setSampleEntropy(request.getSampleEntropy());
        snapshot.setApproximateEntropy(request.getApproximateEntropy());
        snapshot.setDfaAlpha1(request.getDfaAlpha1());
        snapshot.setDfaAlpha2(request.getDfaAlpha2());
        snapshot.setRespiratoryRate(request.getRespiratoryRate());
        snapshot.setEmotionState(request.getEmotionState());
        snapshot.setDeviceStressScore(request.getDeviceStressScore());
        snapshot.setDeviceFatigueScore(request.getDeviceFatigueScore());
        snapshot.setDeviceRecoveryScore(request.getDeviceRecoveryScore());
        snapshot.setMeasureTime(request.getMeasureTime());
        MeridianTimeSlotEnum meridianTimeSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(request.getMeasureTime());
        snapshot.setMeridianTimeSlot(meridianTimeSlot);

        inMemoryDataStore.saveLatestVitalsSnapshot(request.getUserId(), snapshot);
        mergeAndStoreSnapshot(request.getUserId(), request.getMeasureTime());
    }

    /**
     * 接收活动数据。
     *
     * @param request 活动数据请求
     */
    public void ingestActivity(DeviceActivityRequest request) {
        WearableMetricSnapshot snapshot = new WearableMetricSnapshot();
        snapshot.setUserId(request.getUserId());
        snapshot.setStepCount(request.getStepCount());
        snapshot.setCalorieBurn(request.getCalorieBurn());
        snapshot.setMeasureTime(request.getMeasureTime());
        MeridianTimeSlotEnum meridianTimeSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(request.getMeasureTime());
        snapshot.setMeridianTimeSlot(meridianTimeSlot);

        inMemoryDataStore.saveLatestActivitySnapshot(request.getUserId(), snapshot);
        mergeAndStoreSnapshot(request.getUserId(), request.getMeasureTime());
    }

    /**
     * 生成实时评估请求。
     *
     * @param userId 用户标识
     * @return 实时请求
     */
    public HealthStateJudgeRequest buildRealtimeRequest(String userId) {
        WearableMetricSnapshot mergedSnapshot = mergeLatestSnapshot(userId);
        HealthStateJudgeRequest request = new HealthStateJudgeRequest();
        request.setUserId(userId);
        request.setHeartRate(mergedSnapshot.getHeartRate());
        request.setSpo2(mergedSnapshot.getSpo2());
        request.setSd1(mergedSnapshot.getSd1());
        request.setSd2(mergedSnapshot.getSd2());
        request.setHf(mergedSnapshot.getHf());
        request.setLf(mergedSnapshot.getLf());
        request.setVlf(mergedSnapshot.getVlf());
        request.setSampleEntropy(mergedSnapshot.getSampleEntropy());
        request.setApproximateEntropy(mergedSnapshot.getApproximateEntropy());
        request.setDfaAlpha1(mergedSnapshot.getDfaAlpha1());
        request.setDfaAlpha2(mergedSnapshot.getDfaAlpha2());
        request.setRespiratoryRate(mergedSnapshot.getRespiratoryRate());
        request.setStepCount(mergedSnapshot.getStepCount());
        request.setCalorieBurn(mergedSnapshot.getCalorieBurn());
        request.setEmotionState(mergedSnapshot.getEmotionState());
        request.setDeviceStressScore(mergedSnapshot.getDeviceStressScore());
        request.setDeviceFatigueScore(mergedSnapshot.getDeviceFatigueScore());
        request.setDeviceRecoveryScore(mergedSnapshot.getDeviceRecoveryScore());
        request.setMeasureTime(mergedSnapshot.getMeasureTime());
        return request;
    }

    private void mergeAndStoreSnapshot(String userId, java.time.LocalDateTime measureTime) {
        WearableMetricSnapshot mergedSnapshot = mergeLatestSnapshot(userId);
        if (mergedSnapshot.getMeasureTime() == null) {
            mergedSnapshot.setMeasureTime(measureTime);
        }
        MeridianTimeSlotEnum meridianTimeSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(mergedSnapshot.getMeasureTime());
        mergedSnapshot.setMeridianTimeSlot(meridianTimeSlot);
        inMemoryDataStore.addRawInputData(mergedSnapshot);
    }

    private WearableMetricSnapshot mergeLatestSnapshot(String userId) {
        WearableMetricSnapshot vitals = inMemoryDataStore.getLatestVitalsSnapshot(userId);
        WearableMetricSnapshot activity = inMemoryDataStore.getLatestActivitySnapshot(userId);
        WearableMetricSnapshot merged = new WearableMetricSnapshot();
        merged.setUserId(userId);

        if (vitals != null) {
            merged.setHeartRate(vitals.getHeartRate());
            merged.setSpo2(vitals.getSpo2());
            merged.setSd1(vitals.getSd1());
            merged.setSd2(vitals.getSd2());
            merged.setHf(vitals.getHf());
            merged.setLf(vitals.getLf());
            merged.setVlf(vitals.getVlf());
            merged.setSampleEntropy(vitals.getSampleEntropy());
            merged.setApproximateEntropy(vitals.getApproximateEntropy());
            merged.setDfaAlpha1(vitals.getDfaAlpha1());
            merged.setDfaAlpha2(vitals.getDfaAlpha2());
            merged.setRespiratoryRate(vitals.getRespiratoryRate());
            merged.setEmotionState(vitals.getEmotionState());
            merged.setDeviceStressScore(vitals.getDeviceStressScore());
            merged.setDeviceFatigueScore(vitals.getDeviceFatigueScore());
            merged.setDeviceRecoveryScore(vitals.getDeviceRecoveryScore());
            merged.setMeasureTime(vitals.getMeasureTime());
        }

        if (activity != null) {
            merged.setStepCount(activity.getStepCount());
            merged.setCalorieBurn(activity.getCalorieBurn());
            if (merged.getMeasureTime() == null || (activity.getMeasureTime() != null && activity.getMeasureTime().isAfter(merged.getMeasureTime()))) {
                merged.setMeasureTime(activity.getMeasureTime());
            }
        }

        if (merged.getMeasureTime() == null) {
            merged.setMeasureTime(java.time.LocalDateTime.now());
        }
        return merged;
    }
}
