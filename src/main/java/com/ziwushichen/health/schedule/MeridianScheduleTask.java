package com.ziwushichen.health.schedule;

import com.ziwushichen.health.domain.request.HealthStateJudgeRequest;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.service.DeviceDataIngestService;
import com.ziwushichen.health.service.MeridianBaselineBuildService;
import com.ziwushichen.health.service.MeridianRealtimeEvaluateService;
import com.ziwushichen.health.service.MeridianTimeEngine;
import com.ziwushichen.health.service.MeridianTimeslotSummaryService;
import com.ziwushichen.health.store.InMemoryDataStore;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 定时任务组件。
 */
@Component
public class MeridianScheduleTask {

    private final InMemoryDataStore inMemoryDataStore;
    private final DeviceDataIngestService deviceDataIngestService;
    private final MeridianRealtimeEvaluateService meridianRealtimeEvaluateService;
    private final MeridianBaselineBuildService meridianBaselineBuildService;
    private final MeridianTimeslotSummaryService meridianTimeslotSummaryService;
    private final MeridianTimeEngine meridianTimeEngine;

    /**
     * 用户最近结算时辰记录。
     */
    private final Map<String, MeridianTimeSlotEnum> latestSettledSlotMap = new ConcurrentHashMap<>();

    public MeridianScheduleTask(InMemoryDataStore inMemoryDataStore,
                                DeviceDataIngestService deviceDataIngestService,
                                MeridianRealtimeEvaluateService meridianRealtimeEvaluateService,
                                MeridianBaselineBuildService meridianBaselineBuildService,
                                MeridianTimeslotSummaryService meridianTimeslotSummaryService,
                                MeridianTimeEngine meridianTimeEngine) {
        this.inMemoryDataStore = inMemoryDataStore;
        this.deviceDataIngestService = deviceDataIngestService;
        this.meridianRealtimeEvaluateService = meridianRealtimeEvaluateService;
        this.meridianBaselineBuildService = meridianBaselineBuildService;
        this.meridianTimeslotSummaryService = meridianTimeslotSummaryService;
        this.meridianTimeEngine = meridianTimeEngine;
    }

    /**
     * 基线构建任务。
     */
    @Scheduled(fixedDelay = 300000, initialDelay = 120000)
    public void baselineBuildTask() {
        Set<String> userIdSet = inMemoryDataStore.listUserIdSet();
        LocalDateTime now = LocalDateTime.now();
        MeridianTimeSlotEnum currentSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(now);
        for (String userId : userIdSet) {
            meridianBaselineBuildService.buildBaseline(userId, currentSlot, now);
        }
    }

    /**
     * 实时计算任务。
     */
    @Scheduled(fixedDelay = 60000, initialDelay = 60000)
    public void realtimeEvaluateTask() {
        Set<String> userIdSet = inMemoryDataStore.listUserIdSet();
        for (String userId : userIdSet) {
            HealthStateJudgeRequest request = deviceDataIngestService.buildRealtimeRequest(userId);
            meridianRealtimeEvaluateService.evaluate(request);
        }
    }

    /**
     * 时辰结算任务。
     */
    @Scheduled(fixedDelay = 120000, initialDelay = 180000)
    public void timeslotSettleTask() {
        Set<String> userIdSet = inMemoryDataStore.listUserIdSet();
        LocalDateTime now = LocalDateTime.now();
        MeridianTimeSlotEnum currentSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(now);

        for (String userId : userIdSet) {
            MeridianTimeSlotEnum latestSettledSlot = latestSettledSlotMap.get(userId);
            if (latestSettledSlot == null) {
                latestSettledSlotMap.put(userId, currentSlot);
                continue;
            }
            if (latestSettledSlot != currentSlot) {
                meridianTimeslotSummaryService.settle(userId, latestSettledSlot, now);
                latestSettledSlotMap.put(userId, currentSlot);
            }
        }
    }
}
