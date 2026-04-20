package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.entity.MeridianSettlementResult;
import com.ziwushichen.health.domain.entity.RealtimeJudgeResult;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.store.InMemoryDataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 时辰结算服务。
 */
@Service
public class MeridianTimeslotSummaryService {

    private final InMemoryDataStore inMemoryDataStore;
    private final MeridianTimeEngine meridianTimeEngine;

    public MeridianTimeslotSummaryService(InMemoryDataStore inMemoryDataStore,
                                          MeridianTimeEngine meridianTimeEngine) {
        this.inMemoryDataStore = inMemoryDataStore;
        this.meridianTimeEngine = meridianTimeEngine;
    }

    /**
     * 执行指定时辰结算。
     *
     * @param userId 用户标识
     * @param meridianTimeSlot 时辰
     * @param settleTime 结算时间
     * @return 结算结果
     */
    public MeridianSettlementResult settle(String userId,
                                           MeridianTimeSlotEnum meridianTimeSlot,
                                           LocalDateTime settleTime) {
        LocalDateTime slotStartTime = meridianTimeEngine.calculateSlotStartTime(settleTime, meridianTimeSlot);
        LocalDateTime slotEndTime = meridianTimeEngine.calculateSlotEndTime(settleTime, meridianTimeSlot);

        List<WearableMetricSnapshot> fullSlotSnapshotList = loadFullSlotData(userId, slotStartTime, slotEndTime, meridianTimeSlot);

        double avgHeartRate = fullSlotSnapshotList.stream()
                .filter(item -> item.getHeartRate() != null)
                .collect(Collectors.averagingDouble(WearableMetricSnapshot::getHeartRate));
        double avgSpo2 = fullSlotSnapshotList.stream()
                .filter(item -> item.getSpo2() != null)
                .collect(Collectors.averagingDouble(WearableMetricSnapshot::getSpo2));
        double avgStress = fullSlotSnapshotList.stream()
                .filter(item -> item.getDeviceStressScore() != null)
                .collect(Collectors.averagingDouble(item -> item.getDeviceStressScore().doubleValue()));
        String slotAggregatedColorStatus = resolveSlotAggregatedColorStatus(userId, meridianTimeSlot, slotStartTime, slotEndTime);

        MeridianSettlementResult result = new MeridianSettlementResult();
        result.setUserId(userId);
        result.setMeridianTimeSlot(meridianTimeSlot);
        result.setSlotStartTime(slotStartTime);
        result.setSlotEndTime(slotEndTime);
        result.setSampleCount(fullSlotSnapshotList.size());
        result.setSettlementSummary("本时辰共采集" + fullSlotSnapshotList.size()
                + "条数据，平均heartRate=" + format(avgHeartRate)
                + "，平均spo2=" + format(avgSpo2)
                + "，平均deviceStressScore=" + format(avgStress)
                + "，前一时辰汇总状态=" + slotAggregatedColorStatus
                + "，结果用于时辰级健康回顾，不用于疾病诊断。");
        result.setSettlementTime(settleTime);

        inMemoryDataStore.saveMeridianSettlementResult(userId, meridianTimeSlot, result);
        return result;
    }

    private List<WearableMetricSnapshot> loadFullSlotData(String userId,
                                                          LocalDateTime slotStartTime,
                                                          LocalDateTime slotEndTime,
                                                          MeridianTimeSlotEnum meridianTimeSlot) {
        List<WearableMetricSnapshot> candidateList = new ArrayList<>();
        LocalDate date = slotStartTime.toLocalDate();
        while (!date.isAfter(slotEndTime.toLocalDate())) {
            candidateList.addAll(inMemoryDataStore.getRawInputData(userId, date));
            date = date.plusDays(1);
        }

        return candidateList.stream()
                .filter(item -> item.getMeridianTimeSlot() == meridianTimeSlot)
                .filter(item -> !item.getMeasureTime().isBefore(slotStartTime))
                .filter(item -> !item.getMeasureTime().isAfter(slotEndTime))
                .collect(Collectors.toList());
    }

    private String format(double value) {
        return String.format("%.1f", value);
    }

    private String resolveSlotAggregatedColorStatus(String userId,
                                                    MeridianTimeSlotEnum meridianTimeSlot,
                                                    LocalDateTime slotStartTime,
                                                    LocalDateTime slotEndTime) {
        List<RealtimeJudgeResult> historyList = inMemoryDataStore.getRealtimeResultHistory(userId).stream()
                .filter(item -> item.getMeridianTimeSlot() == meridianTimeSlot)
                .filter(item -> !item.getResultTime().isBefore(slotStartTime))
                .filter(item -> !item.getResultTime().isAfter(slotEndTime))
                .collect(Collectors.toList());
        if (historyList.isEmpty()) {
            return "DATA_INSUFFICIENT";
        }

        long redCount = historyList.stream().filter(item -> "IMBALANCE_ALERT".equals(item.getHealthStateJudgeResponse().getHealthColorStatus())).count();
        long grayCount = historyList.stream().filter(item -> "DATA_INSUFFICIENT".equals(item.getHealthStateJudgeResponse().getHealthColorStatus())).count();
        long yellowCount = historyList.stream().filter(item -> "SUB_HEALTH".equals(item.getHealthStateJudgeResponse().getHealthColorStatus())).count();
        if (grayCount >= 2) {
            return "DATA_INSUFFICIENT";
        }
        if (redCount >= 1) {
            return "IMBALANCE_ALERT";
        }
        if (yellowCount >= 1) {
            return "SUB_HEALTH";
        }
        return "HEALTHY";
    }
}
