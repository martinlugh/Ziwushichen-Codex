package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.dto.MeridianScoringResultDTO;
import com.ziwushichen.health.domain.entity.UserBaselineModel;
import com.ziwushichen.health.domain.entity.UserBaselineProfile;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.ActivitySceneTypeEnum;
import com.ziwushichen.health.rule.MeridianRuleConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 时辰健康评分服务。
 */
@Service
public class MeridianHealthScoringService {

    /**
     * 计算评分。
     *
     * @param snapshot 当前窗口聚合快照
     * @param userBaselineModel 用户基线
     * @param meridianRuleConfig 时辰规则
     * @param activityScene 活动场景
     * @param sampleCount 窗口样本数
     * @return 评分结果
     */
    public MeridianScoringResultDTO score(WearableMetricSnapshot snapshot,
                                          UserBaselineModel userBaselineModel,
                                          MeridianRuleConfig meridianRuleConfig,
                                          ActivitySceneTypeEnum activityScene,
                                          int sampleCount) {
        MeridianScoringResultDTO resultDTO = new MeridianScoringResultDTO();
        List<String> mainDrivers = new ArrayList<>();

        double loadScore = calculateLoadScore(snapshot, activityScene, meridianRuleConfig, mainDrivers);
        double recoveryHealthScore = calculateRecoveryHealthScore(snapshot, meridianRuleConfig, mainDrivers);
        double oxygenScore = calculateOxygenScore(snapshot, meridianRuleConfig, mainDrivers);
        double rhythmMatchScore = calculateRhythmMatchScore(snapshot, meridianRuleConfig, mainDrivers);
        double baselineDeviationScore = calculateBaselineDeviationScore(snapshot, userBaselineModel, mainDrivers);
        double confidenceScore = calculateConfidenceScore(sampleCount, userBaselineModel, snapshot, mainDrivers);

        Map<String, Double> weightMap = meridianRuleConfig.getScoreWeight();
        double overallScore = loadScore * weightMap.getOrDefault("loadScore", 0.2D)
                + recoveryHealthScore * weightMap.getOrDefault("recoveryHealthScore", 0.2D)
                + oxygenScore * weightMap.getOrDefault("oxygenScore", 0.15D)
                + rhythmMatchScore * weightMap.getOrDefault("rhythmMatchScore", 0.15D)
                + baselineDeviationScore * weightMap.getOrDefault("baselineDeviationScore", 0.2D)
                + confidenceScore * weightMap.getOrDefault("confidenceScore", 0.1D);

        resultDTO.setLoadScore(clamp(loadScore));
        resultDTO.setRecoveryHealthScore(clamp(recoveryHealthScore));
        resultDTO.setOxygenScore(clamp(oxygenScore));
        resultDTO.setRhythmMatchScore(clamp(rhythmMatchScore));
        resultDTO.setBaselineDeviationScore(clamp(baselineDeviationScore));
        resultDTO.setConfidenceScore(clamp(confidenceScore));
        resultDTO.setOverallScore(clamp(overallScore));
        resultDTO.setMainDrivers(mainDrivers);
        return resultDTO;
    }

    private double calculateLoadScore(WearableMetricSnapshot snapshot,
                                      ActivitySceneTypeEnum activityScene,
                                      MeridianRuleConfig meridianRuleConfig,
                                      List<String> mainDrivers) {
        double score = 100D;
        double heartRate = value(snapshot.getHeartRate());
        double respiratoryRate = value(snapshot.getRespiratoryRate());
        double stepCount = value(snapshot.getStepCount());
        double calorieBurn = value(snapshot.getCalorieBurn());
        double stress = value(snapshot.getDeviceStressScore());

        double heartRateHigh = meridianRuleConfig.getThresholdConfig().getOrDefault("heartRateHigh", 108D);
        if (heartRate > heartRateHigh) {
            score -= 22D;
            mainDrivers.add("heartRate偏高导致负荷上升");
        }
        if (respiratoryRate > 22D) {
            score -= 16D;
            mainDrivers.add("respiratoryRate偏高反映负荷偏大");
        }
        if (stepCount > 120D) {
            score -= 10D;
        }
        if (calorieBurn > 15D) {
            score -= 10D;
        }
        if (stress > meridianRuleConfig.getThresholdConfig().getOrDefault("stressHigh", 78D)) {
            score -= 20D;
            mainDrivers.add("deviceStressScore偏高");
        }
        if (activityScene.name().equals("ACTIVE")) {
            score -= 8D;
        }
        return clamp(score);
    }

    private double calculateRecoveryHealthScore(WearableMetricSnapshot snapshot,
                                                MeridianRuleConfig meridianRuleConfig,
                                                List<String> mainDrivers) {
        double score = 100D;
        double recovery = value(snapshot.getDeviceRecoveryScore());
        double fatigue = value(snapshot.getDeviceFatigueScore());
        double stress = value(snapshot.getDeviceStressScore());

        if (recovery < meridianRuleConfig.getThresholdConfig().getOrDefault("recoveryLow", 40D)) {
            score -= 30D;
            mainDrivers.add("deviceRecoveryScore偏低");
        }
        if (fatigue > 75D) {
            score -= 20D;
            mainDrivers.add("deviceFatigueScore偏高");
        }
        if (stress > meridianRuleConfig.getThresholdConfig().getOrDefault("stressHigh", 78D)) {
            score -= 15D;
        }
        if (isNegativeEmotion(snapshot.getEmotionState())) {
            score -= 10D;
            mainDrivers.add("emotionState提示恢复质量下降");
        }

        if (value(snapshot.getHf()) < 15D || value(snapshot.getLf()) < 15D || value(snapshot.getVlf()) < 15D) {
            score -= 10D;
        }
        return clamp(score);
    }

    private double calculateOxygenScore(WearableMetricSnapshot snapshot,
                                        MeridianRuleConfig meridianRuleConfig,
                                        List<String> mainDrivers) {
        double score = 100D;
        double spo2 = value(snapshot.getSpo2());
        double respiratoryRate = value(snapshot.getRespiratoryRate());
        if (spo2 < meridianRuleConfig.getThresholdConfig().getOrDefault("spo2Low", 94D)) {
            score -= 35D;
            mainDrivers.add("spo2偏低");
        }
        if (respiratoryRate < 10D || respiratoryRate > 24D) {
            score -= 20D;
        }
        return clamp(score);
    }

    private double calculateRhythmMatchScore(WearableMetricSnapshot snapshot,
                                             MeridianRuleConfig meridianRuleConfig,
                                             List<String> mainDrivers) {
        double score = 100D;

        if ("循环负荷".equals(meridianRuleConfig.getFocusDimension())) {
            if (value(snapshot.getHeartRate()) > 110D) {
                score -= 25D;
                mainDrivers.add("午时循环负荷偏高");
            }
        }

        if ("夜间恢复".equals(meridianRuleConfig.getFocusDimension()) || "深度恢复".equals(meridianRuleConfig.getFocusDimension())) {
            if (value(snapshot.getDeviceRecoveryScore()) < 50D) {
                score -= 20D;
            }
            if (value(snapshot.getDeviceStressScore()) > 70D) {
                score -= 20D;
            }
        }

        if (value(snapshot.getDfaAlpha1()) < 0.6D || value(snapshot.getDfaAlpha1()) > 1.6D) {
            score -= 10D;
        }
        if (value(snapshot.getSampleEntropy()) < 0.3D || value(snapshot.getSampleEntropy()) > 2.6D) {
            score -= 10D;
        }
        if (value(snapshot.getApproximateEntropy()) < 0.3D || value(snapshot.getApproximateEntropy()) > 2.6D) {
            score -= 10D;
        }
        return clamp(score);
    }

    private double calculateBaselineDeviationScore(WearableMetricSnapshot snapshot,
                                                   UserBaselineModel userBaselineModel,
                                                   List<String> mainDrivers) {
        UserBaselineProfile.BaselineMetricSnapshot baselineMetricSnapshot = userBaselineModel == null ? null : userBaselineModel.getBaselineMetricSnapshot();
        if (baselineMetricSnapshot == null) {
            mainDrivers.add("缺少基线，基线偏离评分置信降低");
            return 55D;
        }

        List<Double> deviationList = new ArrayList<>();
        deviationList.add(deviationRate(snapshot.getHeartRate(), baselineMetricSnapshot.getHeartRate()));
        deviationList.add(deviationRate(snapshot.getSpo2(), baselineMetricSnapshot.getSpo2()));
        deviationList.add(deviationRate(snapshot.getRespiratoryRate(), baselineMetricSnapshot.getRespiratoryRate()));
        deviationList.add(deviationRate(snapshot.getSd1(), baselineMetricSnapshot.getSd1()));
        deviationList.add(deviationRate(snapshot.getSd2(), baselineMetricSnapshot.getSd2()));
        deviationList.add(deviationRate(snapshot.getHf(), baselineMetricSnapshot.getHf()));
        deviationList.add(deviationRate(snapshot.getLf(), baselineMetricSnapshot.getLf()));
        deviationList.add(deviationRate(snapshot.getVlf(), baselineMetricSnapshot.getVlf()));
        deviationList.add(deviationRate(snapshot.getSampleEntropy(), baselineMetricSnapshot.getSampleEntropy()));
        deviationList.add(deviationRate(snapshot.getApproximateEntropy(), baselineMetricSnapshot.getApproximateEntropy()));
        deviationList.add(deviationRate(snapshot.getDfaAlpha1(), baselineMetricSnapshot.getDfaAlpha1()));
        deviationList.add(deviationRate(snapshot.getDfaAlpha2(), baselineMetricSnapshot.getDfaAlpha2()));
        deviationList.add(deviationRate(toDouble(snapshot.getStepCount()), toDouble(baselineMetricSnapshot.getStepCount())));
        deviationList.add(deviationRate(snapshot.getCalorieBurn(), baselineMetricSnapshot.getCalorieBurn()));
        deviationList.add(deviationRate(toDouble(snapshot.getDeviceStressScore()), toDouble(baselineMetricSnapshot.getDeviceStressScore())));
        deviationList.add(deviationRate(toDouble(snapshot.getDeviceFatigueScore()), toDouble(baselineMetricSnapshot.getDeviceFatigueScore())));
        deviationList.add(deviationRate(toDouble(snapshot.getDeviceRecoveryScore()), toDouble(baselineMetricSnapshot.getDeviceRecoveryScore())));

        double avgDeviationRate = deviationList.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
        double score = 100D - avgDeviationRate * 120D;
        if (avgDeviationRate > 0.25D) {
            mainDrivers.add("多项指标偏离个人基线");
        }
        return clamp(score);
    }

    private double calculateConfidenceScore(int sampleCount,
                                            UserBaselineModel userBaselineModel,
                                            WearableMetricSnapshot snapshot,
                                            List<String> mainDrivers) {
        double score = 100D;
        if (sampleCount < 3) {
            score -= 35D;
            mainDrivers.add("当前窗口样本数量偏少");
        }
        if (userBaselineModel == null) {
            score -= 20D;
            mainDrivers.add("尚未建立个人基线");
        }

        int missingCount = 0;
        missingCount += snapshot.getHeartRate() == null ? 1 : 0;
        missingCount += snapshot.getSpo2() == null ? 1 : 0;
        missingCount += snapshot.getRespiratoryRate() == null ? 1 : 0;
        missingCount += snapshot.getSd1() == null ? 1 : 0;
        missingCount += snapshot.getSd2() == null ? 1 : 0;
        missingCount += snapshot.getHf() == null ? 1 : 0;
        missingCount += snapshot.getLf() == null ? 1 : 0;
        missingCount += snapshot.getVlf() == null ? 1 : 0;
        missingCount += snapshot.getSampleEntropy() == null ? 1 : 0;
        missingCount += snapshot.getApproximateEntropy() == null ? 1 : 0;
        missingCount += snapshot.getDfaAlpha1() == null ? 1 : 0;
        missingCount += snapshot.getDfaAlpha2() == null ? 1 : 0;
        missingCount += snapshot.getDeviceStressScore() == null ? 1 : 0;
        missingCount += snapshot.getDeviceFatigueScore() == null ? 1 : 0;
        missingCount += snapshot.getDeviceRecoveryScore() == null ? 1 : 0;
        score -= Math.min(30D, missingCount * 2D);

        return clamp(score);
    }

    private double deviationRate(Double current, Double baseline) {
        if (current == null || baseline == null || baseline == 0D) {
            return 0.2D;
        }
        return Math.abs(current - baseline) / Math.abs(baseline);
    }

    private double value(Double value) {
        return value == null ? 0D : value;
    }

    private Double toDouble(Integer value) {
        return value == null ? null : value.doubleValue();
    }

    private boolean isNegativeEmotion(String emotionState) {
        if (emotionState == null) {
            return false;
        }
        String normalized = emotionState.toUpperCase();
        return normalized.contains("不愉悦") || normalized.contains("UNPLEASANT");
    }

    private double clamp(double score) {
        return Math.max(0D, Math.min(100D, score));
    }
}
