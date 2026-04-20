package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.entity.BaselineModelProgress;
import com.ziwushichen.health.domain.entity.MeridianSettlementResult;
import com.ziwushichen.health.domain.entity.RealtimeJudgeResult;
import com.ziwushichen.health.domain.entity.UserBaselineModel;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.domain.request.HealthStateJudgeRequest;
import com.ziwushichen.health.domain.response.HealthStateJudgeResponse;
import com.ziwushichen.health.domain.response.api.DailySummaryResponse;
import com.ziwushichen.health.domain.response.api.ModelProgressResponse;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.store.InMemoryDataStore;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 对外开放服务。
 */
@Service
public class MeridianOpenApiService {

    private final DeviceDataIngestService deviceDataIngestService;
    private final MeridianRealtimeEvaluateService meridianRealtimeEvaluateService;
    private final MeridianTimeslotSummaryService meridianTimeslotSummaryService;
    private final MeridianBaselineBuildService meridianBaselineBuildService;
    private final MeridianTimeEngine meridianTimeEngine;
    private final InMemoryDataStore inMemoryDataStore;

    public MeridianOpenApiService(DeviceDataIngestService deviceDataIngestService,
                                  MeridianRealtimeEvaluateService meridianRealtimeEvaluateService,
                                  MeridianTimeslotSummaryService meridianTimeslotSummaryService,
                                  MeridianBaselineBuildService meridianBaselineBuildService,
                                  MeridianTimeEngine meridianTimeEngine,
                                  InMemoryDataStore inMemoryDataStore) {
        this.deviceDataIngestService = deviceDataIngestService;
        this.meridianRealtimeEvaluateService = meridianRealtimeEvaluateService;
        this.meridianTimeslotSummaryService = meridianTimeslotSummaryService;
        this.meridianBaselineBuildService = meridianBaselineBuildService;
        this.meridianTimeEngine = meridianTimeEngine;
        this.inMemoryDataStore = inMemoryDataStore;
    }

    /**
     * 查询当前实时状态。
     *
     * @param userId 用户标识
     * @return 实时结果
     */
    public HealthStateJudgeResponse getRealtimeCurrent(String userId) {
        HealthStateJudgeRequest request = deviceDataIngestService.buildRealtimeRequest(userId);
        meridianRealtimeEvaluateService.evaluate(request);
        return meridianRealtimeEvaluateService.aggregateThirtyMinuteStatus(userId, LocalDateTime.now());
    }

    /**
     * 查询时辰结算。
     *
     * @param userId 用户标识
     * @param meridianTimeSlot 时辰
     * @return 时辰结算
     */
    public MeridianSettlementResult getTimeslotSummary(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        MeridianTimeSlotEnum targetSlot = meridianTimeSlot;
        if (targetSlot == null) {
            targetSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(LocalDateTime.now());
        }

        MeridianSettlementResult existing = inMemoryDataStore.getMeridianSettlementResult(userId, targetSlot);
        if (existing != null) {
            return existing;
        }
        return meridianTimeslotSummaryService.settle(userId, targetSlot, LocalDateTime.now());
    }

    /**
     * 查询每日总结。
     *
     * @param userId 用户标识
     * @param date 日期
     * @return 每日总结
     */
    public DailySummaryResponse getDailySummary(String userId, LocalDate date) {
        LocalDate targetDate = date == null ? LocalDate.now() : date;
        List<WearableMetricSnapshot> snapshotList = inMemoryDataStore.getRawInputData(userId, targetDate);
        List<HealthStateJudgeResponse> realtimeResponseList = new ArrayList<>();

        RealtimeJudgeResult realtimeJudgeResult = inMemoryDataStore.getRealtimeResult(userId);
        if (realtimeJudgeResult != null && realtimeJudgeResult.getHealthStateJudgeResponse() != null) {
            realtimeResponseList.add(realtimeJudgeResult.getHealthStateJudgeResponse());
        }

        double avgOverallScore = realtimeResponseList.stream()
                .filter(item -> item.getOverallScore() != null)
                .mapToDouble(HealthStateJudgeResponse::getOverallScore)
                .average()
                .orElse(0D);

        DailySummaryResponse response = new DailySummaryResponse();
        response.setUserId(userId);
        response.setDate(targetDate.toString());
        response.setSampleCount(snapshotList.size());
        response.setAvgOverallScore(avgOverallScore);

        List<String> keyObservationList = new ArrayList<>();
        if (avgOverallScore >= 80D) {
            response.setDailyConclusion("当日整体状态稳定，节律表现良好。");
            keyObservationList.add("整体负荷可控，恢复质量较好");
        } else if (avgOverallScore >= 60D) {
            response.setDailyConclusion("当日存在轻度波动，建议继续观察。");
            keyObservationList.add("部分时段出现恢复不足或负荷上升");
        } else {
            response.setDailyConclusion("当日波动较明显，建议强化恢复管理。");
            keyObservationList.add("建议重点关注睡眠前后与午时状态");
        }
        response.setKeyObservations(keyObservationList);
        return response;
    }

    /**
     * 重建基线。
     *
     * @param userId 用户标识
     * @param meridianTimeSlot 时辰
     * @return 基线模型
     */
    public UserBaselineModel rebuildBaseline(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        MeridianTimeSlotEnum targetSlot = meridianTimeSlot;
        if (targetSlot == null) {
            targetSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(LocalDateTime.now());
        }
        return meridianBaselineBuildService.buildBaseline(userId, targetSlot, LocalDateTime.now());
    }

    /**
     * 查询建模进度。
     *
     * @param userId 用户标识
     * @param meridianTimeSlot 时辰
     * @return 建模进度
     */
    public ModelProgressResponse getModelProgress(String userId, MeridianTimeSlotEnum meridianTimeSlot) {
        MeridianTimeSlotEnum targetSlot = meridianTimeSlot;
        if (targetSlot == null) {
            targetSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(LocalDateTime.now());
        }
        BaselineModelProgress modelProgress = inMemoryDataStore.getBaselineModelProgress(userId, targetSlot);
        if (modelProgress == null) {
            UserBaselineModel userBaselineModel = rebuildBaseline(userId, targetSlot);
            modelProgress = inMemoryDataStore.getBaselineModelProgress(userId, targetSlot);
            if (modelProgress == null) {
                modelProgress = new BaselineModelProgress();
                modelProgress.setUserId(userId);
                modelProgress.setMeridianTimeSlot(targetSlot);
                modelProgress.setCandidateCount(0);
                modelProgress.setEligibleCount(0);
                modelProgress.setCurrentType(userBaselineModel.getBaselineType());
                modelProgress.setProgressRemark("已完成最小化建模");
                modelProgress.setUpdateTime(LocalDateTime.now());
            }
        }

        ModelProgressResponse response = new ModelProgressResponse();
        response.setUserId(modelProgress.getUserId());
        response.setMeridianTimeSlot(modelProgress.getMeridianTimeSlot());
        response.setCandidateCount(modelProgress.getCandidateCount());
        response.setEligibleCount(modelProgress.getEligibleCount());
        response.setCurrentType(modelProgress.getCurrentType());
        response.setProgressRemark(modelProgress.getProgressRemark());
        response.setUpdateTime(modelProgress.getUpdateTime());
        return response;
    }
}
