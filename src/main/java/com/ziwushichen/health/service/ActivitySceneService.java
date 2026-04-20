package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.ActivitySceneTypeEnum;
import org.springframework.stereotype.Service;

/**
 * 活动场景识别服务。
 */
@Service
public class ActivitySceneService {

    /**
     * 识别活动场景。
     * 仅基于外部输入参数进行判断，不派生新的复杂指标。
     *
     * @param snapshot 指标快照
     * @return 活动场景
     */
    public ActivitySceneTypeEnum identifyActivityScene(WearableMetricSnapshot snapshot) {
        double heartRate = value(snapshot.getHeartRate());
        double respiratoryRate = value(snapshot.getRespiratoryRate());
        int stepCount = intValue(snapshot.getStepCount());
        double calorieBurn = value(snapshot.getCalorieBurn());
        int deviceStressScore = intValue(snapshot.getDeviceStressScore());
        int deviceRecoveryScore = intValue(snapshot.getDeviceRecoveryScore());

        if (isPostActivityRecovery(heartRate, respiratoryRate, stepCount, calorieBurn, deviceRecoveryScore)) {
            return ActivitySceneTypeEnum.POST_ACTIVITY_RECOVERY;
        }
        if (isActive(heartRate, respiratoryRate, stepCount, calorieBurn, deviceStressScore)) {
            return ActivitySceneTypeEnum.ACTIVE;
        }
        if (isLightActivity(heartRate, respiratoryRate, stepCount, calorieBurn)) {
            return ActivitySceneTypeEnum.LIGHT_ACTIVITY;
        }
        return ActivitySceneTypeEnum.REST;
    }

    private boolean isPostActivityRecovery(double heartRate,
                                           double respiratoryRate,
                                           int stepCount,
                                           double calorieBurn,
                                           int deviceRecoveryScore) {
        return stepCount < 60
                && calorieBurn < 8
                && heartRate >= 72 && heartRate <= 95
                && respiratoryRate >= 12 && respiratoryRate <= 20
                && deviceRecoveryScore >= 70;
    }

    private boolean isActive(double heartRate,
                             double respiratoryRate,
                             int stepCount,
                             double calorieBurn,
                             int deviceStressScore) {
        return heartRate >= 105
                || respiratoryRate >= 22
                || stepCount >= 150
                || calorieBurn >= 18
                || deviceStressScore >= 70;
    }

    private boolean isLightActivity(double heartRate,
                                    double respiratoryRate,
                                    int stepCount,
                                    double calorieBurn) {
        return heartRate >= 85
                || respiratoryRate >= 18
                || stepCount >= 60
                || calorieBurn >= 8;
    }

    private double value(Double number) {
        return number == null ? 0D : number;
    }

    private int intValue(Integer number) {
        return number == null ? 0 : number;
    }
}
