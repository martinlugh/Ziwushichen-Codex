package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.dto.BaselineHealthFilterResultDTO;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.ActivitySceneTypeEnum;
import com.ziwushichen.health.enums.BaselineEligibilityLevelEnum;
import com.ziwushichen.health.enums.BaselineModelTypeEnum;
import com.ziwushichen.health.enums.ConfidenceLevelEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 基线健康筛选服务。
 */
@Service
public class BaselineHealthFilterService {

    /**
     * 执行基线筛选。
     *
     * @param snapshot 指标快照
     * @param activityScene 活动场景
     * @return 筛选结果
     */
    public BaselineHealthFilterResultDTO filter(WearableMetricSnapshot snapshot, ActivitySceneTypeEnum activityScene) {
        List<String> baselineRejectReasons = new ArrayList<>();
        double baselineHealthScore = 100D;

        baselineHealthScore = baselineHealthScore - evaluatePhysiologicalStability(snapshot, baselineRejectReasons);
        baselineHealthScore = baselineHealthScore - evaluateAutonomicAndRecovery(snapshot, baselineRejectReasons);
        baselineHealthScore = baselineHealthScore - evaluateActivityInterference(snapshot, activityScene, baselineRejectReasons);
        baselineHealthScore = baselineHealthScore - evaluateMeridianRationality(snapshot, baselineRejectReasons);

        if (baselineHealthScore < 0D) {
            baselineHealthScore = 0D;
        }

        BaselineHealthFilterResultDTO resultDTO = new BaselineHealthFilterResultDTO();
        resultDTO.setBaselineHealthScore(baselineHealthScore);
        resultDTO.setBaselineRejectReasons(baselineRejectReasons);
        resultDTO.setConfidenceLevel(resolveConfidenceLevel(baselineHealthScore));

        if (baselineHealthScore >= 80D && baselineRejectReasons.size() <= 2) {
            resultDTO.setBaselineEligibilityLevel(BaselineEligibilityLevelEnum.FULLY_ELIGIBLE);
            resultDTO.setBaselineTypeUsed(BaselineModelTypeEnum.PERSONAL_HEALTHY);
        } else if (baselineHealthScore >= 60D) {
            resultDTO.setBaselineEligibilityLevel(BaselineEligibilityLevelEnum.PARTIALLY_ELIGIBLE);
            resultDTO.setBaselineTypeUsed(BaselineModelTypeEnum.PERSONAL_STABLE);
        } else {
            resultDTO.setBaselineEligibilityLevel(BaselineEligibilityLevelEnum.NOT_ELIGIBLE);
            resultDTO.setBaselineTypeUsed(BaselineModelTypeEnum.GENERAL_REFERENCE);
        }
        return resultDTO;
    }

    private double evaluatePhysiologicalStability(WearableMetricSnapshot snapshot, List<String> rejectReasons) {
        double penalty = 0D;

        penalty += evaluateRange(snapshot.getHeartRate(), 50D, 105D, 10D, "heartRate超出生理稳定范围", rejectReasons);
        penalty += evaluateRange(snapshot.getSpo2(), 93D, 100D, 10D, "spo2偏离稳定范围", rejectReasons);
        penalty += evaluateRange(snapshot.getRespiratoryRate(), 10D, 24D, 10D, "respiratoryRate偏离稳定范围", rejectReasons);
        penalty += evaluateRange(snapshot.getSd1(), 5D, 120D, 6D, "sd1异常", rejectReasons);
        penalty += evaluateRange(snapshot.getSd2(), 10D, 240D, 6D, "sd2异常", rejectReasons);
        penalty += evaluateRange(snapshot.getSampleEntropy(), 0.3D, 2.5D, 6D, "sampleEntropy异常", rejectReasons);
        penalty += evaluateRange(snapshot.getApproximateEntropy(), 0.3D, 2.5D, 6D, "approximateEntropy异常", rejectReasons);
        penalty += evaluateRange(snapshot.getDfaAlpha1(), 0.5D, 1.6D, 6D, "dfaAlpha1异常", rejectReasons);
        penalty += evaluateRange(snapshot.getDfaAlpha2(), 0.5D, 1.8D, 6D, "dfaAlpha2异常", rejectReasons);

        return penalty;
    }

    private double evaluateAutonomicAndRecovery(WearableMetricSnapshot snapshot, List<String> rejectReasons) {
        double penalty = 0D;

        penalty += evaluateRange(intToDouble(snapshot.getDeviceStressScore()), 0D, 75D, 8D, "deviceStressScore偏高", rejectReasons);
        penalty += evaluateRange(intToDouble(snapshot.getDeviceFatigueScore()), 0D, 75D, 8D, "deviceFatigueScore偏高", rejectReasons);
        penalty += evaluateRange(intToDouble(snapshot.getDeviceRecoveryScore()), 35D, 100D, 10D, "deviceRecoveryScore偏低", rejectReasons);

        penalty += evaluateRange(snapshot.getHf(), 5D, 5000D, 6D, "hf异常", rejectReasons);
        penalty += evaluateRange(snapshot.getLf(), 5D, 5000D, 6D, "lf异常", rejectReasons);
        penalty += evaluateRange(snapshot.getVlf(), 5D, 5000D, 6D, "vlf异常", rejectReasons);

        if (isNegativeEmotion(snapshot.getEmotionState())) {
            penalty += 6D;
            rejectReasons.add("emotionState显示负性情绪");
        }

        return penalty;
    }

    private double evaluateActivityInterference(WearableMetricSnapshot snapshot,
                                                ActivitySceneTypeEnum activityScene,
                                                List<String> rejectReasons) {
        double penalty = 0D;

        if (activityScene == ActivitySceneTypeEnum.ACTIVE) {
            penalty += 15D;
            rejectReasons.add("activityScene为ACTIVE，活动干扰较强");
        }

        penalty += evaluateRange(intToDouble(snapshot.getStepCount()), 0D, 120D, 5D, "stepCount过高", rejectReasons);
        penalty += evaluateRange(snapshot.getCalorieBurn(), 0D, 15D, 5D, "calorieBurn过高", rejectReasons);
        return penalty;
    }

    private double evaluateMeridianRationality(WearableMetricSnapshot snapshot, List<String> rejectReasons) {
        MeridianTimeSlotEnum meridianTimeSlot = snapshot.getMeridianTimeSlot();
        if (meridianTimeSlot == null) {
            rejectReasons.add("缺少时辰信息");
            return 12D;
        }

        double penalty = 0D;

        if (meridianTimeSlot == MeridianTimeSlotEnum.ZI || meridianTimeSlot == MeridianTimeSlotEnum.CHOU
                || meridianTimeSlot == MeridianTimeSlotEnum.YIN || meridianTimeSlot == MeridianTimeSlotEnum.HAI) {
            penalty += evaluateRange(intToDouble(snapshot.getDeviceRecoveryScore()), 50D, 100D, 6D, "夜间恢复不足", rejectReasons);
        }

        if (meridianTimeSlot == MeridianTimeSlotEnum.WU) {
            penalty += evaluateRange(snapshot.getHeartRate(), 50D, 110D, 6D, "午时循环负荷异常", rejectReasons);
        }

        if (meridianTimeSlot == MeridianTimeSlotEnum.XU || meridianTimeSlot == MeridianTimeSlotEnum.HAI) {
            penalty += evaluateRange(snapshot.getRespiratoryRate(), 10D, 20D, 5D, "晚间呼吸回落不足", rejectReasons);
        }

        if (meridianTimeSlot == MeridianTimeSlotEnum.CHEN || meridianTimeSlot == MeridianTimeSlotEnum.SI
                || meridianTimeSlot == MeridianTimeSlotEnum.SHEN || meridianTimeSlot == MeridianTimeSlotEnum.YOU) {
            penalty += evaluateRange(snapshot.getDfaAlpha1(), 0.7D, 1.4D, 5D, "白天节律匹配不足", rejectReasons);
        }

        return penalty;
    }

    private double evaluateRange(Double value,
                                 Double min,
                                 Double max,
                                 double penalty,
                                 String rejectReason,
                                 List<String> rejectReasons) {
        if (value == null) {
            rejectReasons.add(rejectReason + "，字段为空");
            return penalty;
        }
        if (value < min || value > max) {
            rejectReasons.add(rejectReason);
            return penalty;
        }
        return 0D;
    }

    private double intToDouble(Integer value) {
        if (value == null) {
            return Double.NaN;
        }
        return value.doubleValue();
    }

    private boolean isNegativeEmotion(String emotionState) {
        if (emotionState == null) {
            return false;
        }
        String normalized = emotionState.toUpperCase(Locale.ROOT);
        return normalized.contains("不愉悦") || normalized.contains("UNPLEASANT");
    }

    private ConfidenceLevelEnum resolveConfidenceLevel(double baselineHealthScore) {
        if (baselineHealthScore >= 80D) {
            return ConfidenceLevelEnum.HIGH;
        }
        if (baselineHealthScore >= 60D) {
            return ConfidenceLevelEnum.MEDIUM;
        }
        return ConfidenceLevelEnum.LOW;
    }
}
