package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.dto.HealthColorDecisionResultDTO;
import com.ziwushichen.health.domain.dto.MeridianScoringResultDTO;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.HealthColorStatusEnum;
import com.ziwushichen.health.store.InMemoryDataStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 健康颜色判定服务。
 */
@Service
public class HealthColorDecisionService {

    private final InMemoryDataStore inMemoryDataStore;

    public HealthColorDecisionService(InMemoryDataStore inMemoryDataStore) {
        this.inMemoryDataStore = inMemoryDataStore;
    }

    /**
     * 执行颜色决策。
     *
     * @param userId 用户标识
     * @param scoringResultDTO 评分结果
     * @param snapshot 当前窗口聚合快照
     * @param previousColor 上一颜色状态
     * @return 决策结果
     */
    public HealthColorDecisionResultDTO decide(String userId,
                                               MeridianScoringResultDTO scoringResultDTO,
                                               WearableMetricSnapshot snapshot,
                                               HealthColorStatusEnum previousColor) {
        HealthColorDecisionResultDTO resultDTO = new HealthColorDecisionResultDTO();
        List<String> mainDrivers = new ArrayList<>(scoringResultDTO.getMainDrivers());

        if (isDataInsufficient(snapshot, scoringResultDTO.getConfidenceScore())) {
            resultDTO.setHealthColorStatus(HealthColorStatusEnum.DATA_INSUFFICIENT);
            resultDTO.setMainDrivers(mainDrivers);
            resultDTO.setColorChangeReason("关键数据缺失或置信度过低，优先判定为数据不足");
            inMemoryDataStore.addColorStatusHistory(userId, HealthColorStatusEnum.DATA_INSUFFICIENT);
            return resultDTO;
        }

        if (isCriticalAnomaly(snapshot, scoringResultDTO)) {
            resultDTO.setHealthColorStatus(HealthColorStatusEnum.IMBALANCE_ALERT);
            mainDrivers.add("存在关键异常一票否决");
            resultDTO.setMainDrivers(mainDrivers);
            resultDTO.setColorChangeReason("触发关键异常阈值，进入失衡预警状态");
            inMemoryDataStore.addColorStatusHistory(userId, HealthColorStatusEnum.IMBALANCE_ALERT);
            return resultDTO;
        }

        double riskRatio = calculateRiskRatio(scoringResultDTO);
        int persistentRiskCount = calculatePersistentRiskCount(userId);

        HealthColorStatusEnum targetColor;
        if (scoringResultDTO.getOverallScore() >= 80D && riskRatio < 0.35D) {
            targetColor = HealthColorStatusEnum.HEALTHY;
        } else if (scoringResultDTO.getOverallScore() >= 60D && riskRatio < 0.60D) {
            targetColor = HealthColorStatusEnum.SUB_HEALTH;
        } else {
            targetColor = HealthColorStatusEnum.IMBALANCE_ALERT;
        }

        HealthColorStatusEnum finalColor = applyAntiJitter(previousColor, targetColor, persistentRiskCount, scoringResultDTO.getOverallScore());
        resultDTO.setHealthColorStatus(finalColor);

        if (finalColor == HealthColorStatusEnum.HEALTHY) {
            resultDTO.setColorChangeReason("评分稳定且风险占比较低，维持健康状态");
        } else if (finalColor == HealthColorStatusEnum.SUB_HEALTH) {
            resultDTO.setColorChangeReason("存在一定比例风险指标，进入亚健康观察状态");
        } else {
            resultDTO.setColorChangeReason("风险占比与持续性达到阈值，进入失衡预警状态");
        }

        resultDTO.setMainDrivers(mainDrivers);
        inMemoryDataStore.addColorStatusHistory(userId, finalColor);
        return resultDTO;
    }

    private boolean isDataInsufficient(WearableMetricSnapshot snapshot, double confidenceScore) {
        int keyMissingCount = 0;
        keyMissingCount += snapshot.getHeartRate() == null ? 1 : 0;
        keyMissingCount += snapshot.getSpo2() == null ? 1 : 0;
        keyMissingCount += snapshot.getRespiratoryRate() == null ? 1 : 0;
        keyMissingCount += snapshot.getDeviceStressScore() == null ? 1 : 0;
        keyMissingCount += snapshot.getDeviceRecoveryScore() == null ? 1 : 0;
        return keyMissingCount >= 2 || confidenceScore < 45D;
    }

    private boolean isCriticalAnomaly(WearableMetricSnapshot snapshot, MeridianScoringResultDTO scoringResultDTO) {
        return value(snapshot.getSpo2()) < 90D
                || value(snapshot.getHeartRate()) > 130D
                || value(snapshot.getRespiratoryRate()) > 30D
                || value(snapshot.getDeviceStressScore()) > 90D
                || value(snapshot.getDeviceRecoveryScore()) < 20D
                || scoringResultDTO.getOxygenScore() < 40D;
    }

    private double calculateRiskRatio(MeridianScoringResultDTO scoringResultDTO) {
        int riskCount = 0;
        if (scoringResultDTO.getLoadScore() < 60D) {
            riskCount++;
        }
        if (scoringResultDTO.getRecoveryHealthScore() < 60D) {
            riskCount++;
        }
        if (scoringResultDTO.getOxygenScore() < 60D) {
            riskCount++;
        }
        if (scoringResultDTO.getRhythmMatchScore() < 60D) {
            riskCount++;
        }
        if (scoringResultDTO.getBaselineDeviationScore() < 60D) {
            riskCount++;
        }
        if (scoringResultDTO.getConfidenceScore() < 60D) {
            riskCount++;
        }
        return riskCount / 6D;
    }

    private int calculatePersistentRiskCount(String userId) {
        List<HealthColorStatusEnum> historyList = inMemoryDataStore.getColorStatusHistory(userId);
        int count = 0;
        for (int i = historyList.size() - 1; i >= 0 && i >= historyList.size() - 3; i--) {
            HealthColorStatusEnum statusEnum = historyList.get(i);
            if (statusEnum == HealthColorStatusEnum.IMBALANCE_ALERT || statusEnum == HealthColorStatusEnum.SUB_HEALTH) {
                count++;
            }
        }
        return count;
    }

    private HealthColorStatusEnum applyAntiJitter(HealthColorStatusEnum previousColor,
                                                   HealthColorStatusEnum targetColor,
                                                   int persistentRiskCount,
                                                   double overallScore) {
        if (previousColor == null) {
            return targetColor;
        }

        if (targetColor == HealthColorStatusEnum.IMBALANCE_ALERT) {
            return HealthColorStatusEnum.IMBALANCE_ALERT;
        }

        if (previousColor == HealthColorStatusEnum.IMBALANCE_ALERT && targetColor == HealthColorStatusEnum.SUB_HEALTH) {
            if (overallScore < 70D || persistentRiskCount >= 2) {
                return HealthColorStatusEnum.IMBALANCE_ALERT;
            }
        }

        if (previousColor == HealthColorStatusEnum.SUB_HEALTH && targetColor == HealthColorStatusEnum.HEALTHY) {
            if (overallScore < 85D || persistentRiskCount >= 1) {
                return HealthColorStatusEnum.SUB_HEALTH;
            }
        }

        if (previousColor == HealthColorStatusEnum.IMBALANCE_ALERT && targetColor == HealthColorStatusEnum.HEALTHY) {
            if (overallScore < 88D || persistentRiskCount >= 1) {
                return HealthColorStatusEnum.SUB_HEALTH;
            }
        }

        return targetColor;
    }

    private double value(Double value) {
        return value == null ? 0D : value;
    }
}
