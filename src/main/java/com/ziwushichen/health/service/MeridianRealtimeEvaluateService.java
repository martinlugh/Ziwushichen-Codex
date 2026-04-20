package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.dto.HealthColorDecisionResultDTO;
import com.ziwushichen.health.domain.dto.MeridianTimeContextDTO;
import com.ziwushichen.health.domain.dto.MeridianScoringResultDTO;
import com.ziwushichen.health.domain.entity.MeridianWindowSegment;
import com.ziwushichen.health.domain.entity.RealtimeJudgeResult;
import com.ziwushichen.health.domain.entity.UserBaselineModel;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.domain.request.HealthStateJudgeRequest;
import com.ziwushichen.health.domain.response.HealthStateJudgeResponse;
import com.ziwushichen.health.enums.ActivitySceneTypeEnum;
import com.ziwushichen.health.enums.HealthColorStatusEnum;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.rule.MeridianRuleConfig;
import com.ziwushichen.health.rule.MeridianRuleConfigRegistry;
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
 * 时辰实时评估服务。
 */
@Service
public class MeridianRealtimeEvaluateService {

    private final InMemoryDataStore inMemoryDataStore;
    private final MeridianTimeEngine meridianTimeEngine;
    private final ActivitySceneService activitySceneService;
    private final MeridianBaselineBuildService meridianBaselineBuildService;
    private final MeridianHealthScoringService meridianHealthScoringService;
    private final HealthColorDecisionService healthColorDecisionService;
    private final MeridianCopywritingService meridianCopywritingService;
    private final MeridianRuleConfigRegistry meridianRuleConfigRegistry;
    private final MeridianTimeslotSummaryService meridianTimeslotSummaryService;

    public MeridianRealtimeEvaluateService(InMemoryDataStore inMemoryDataStore,
                                           MeridianTimeEngine meridianTimeEngine,
                                           ActivitySceneService activitySceneService,
                                           MeridianBaselineBuildService meridianBaselineBuildService,
                                           MeridianHealthScoringService meridianHealthScoringService,
                                           HealthColorDecisionService healthColorDecisionService,
                                           MeridianCopywritingService meridianCopywritingService,
                                           MeridianRuleConfigRegistry meridianRuleConfigRegistry,
                                           MeridianTimeslotSummaryService meridianTimeslotSummaryService) {
        this.inMemoryDataStore = inMemoryDataStore;
        this.meridianTimeEngine = meridianTimeEngine;
        this.activitySceneService = activitySceneService;
        this.meridianBaselineBuildService = meridianBaselineBuildService;
        this.meridianHealthScoringService = meridianHealthScoringService;
        this.healthColorDecisionService = healthColorDecisionService;
        this.meridianCopywritingService = meridianCopywritingService;
        this.meridianRuleConfigRegistry = meridianRuleConfigRegistry;
        this.meridianTimeslotSummaryService = meridianTimeslotSummaryService;
    }

    /**
     * 执行实时评估。
     *
     * @param request 输入请求
     * @return 实时响应
     */
    public HealthStateJudgeResponse evaluate(HealthStateJudgeRequest request) {
        WearableMetricSnapshot currentSnapshot = convertToSnapshot(request);
        MeridianTimeSlotEnum meridianTimeSlot = meridianTimeEngine.resolveCurrentMeridianTimeSlot(request.getMeasureTime());
        currentSnapshot.setMeridianTimeSlot(meridianTimeSlot);
        inMemoryDataStore.addRawInputData(currentSnapshot);

        RealtimeJudgeResult previousResult = inMemoryDataStore.getRealtimeResult(request.getUserId());
        MeridianTimeSlotEnum previousMeridianTimeSlot = previousResult == null ? null : previousResult.getMeridianTimeSlot();
        MeridianTimeContextDTO timeContextDTO = meridianTimeEngine.buildMeridianTimeContext(request.getMeasureTime(), previousMeridianTimeSlot);
        if (timeContextDTO.isNeedSettlePreviousSlot() && timeContextDTO.getPreviousMeridianTimeSlot() != null) {
            meridianTimeslotSummaryService.settle(request.getUserId(),
                    timeContextDTO.getPreviousMeridianTimeSlot(),
                    request.getMeasureTime());
        }

        List<WearableMetricSnapshot> candidateList = loadCrossDateCandidateList(request.getUserId(), request.getMeasureTime());
        MeridianWindowSegment windowSegment = meridianTimeEngine.buildCurrentEffectiveWindow(request.getUserId(), request.getMeasureTime(), candidateList);
        inMemoryDataStore.saveMeridianWindowSegment(request.getUserId(), meridianTimeSlot, windowSegment);

        WearableMetricSnapshot windowSnapshot = aggregateWindowSnapshot(request.getUserId(), windowSegment.getMetricSnapshotList(), meridianTimeSlot, request.getMeasureTime());
        ActivitySceneTypeEnum activityScene = activitySceneService.identifyActivityScene(windowSnapshot);

        UserBaselineModel userBaselineModel = inMemoryDataStore.getUserBaseline(request.getUserId(), meridianTimeSlot);
        if (userBaselineModel == null) {
            userBaselineModel = meridianBaselineBuildService.buildBaseline(request.getUserId(), meridianTimeSlot, request.getMeasureTime());
        }

        MeridianRuleConfig ruleConfig = meridianRuleConfigRegistry.getBySlot(meridianTimeSlot);
        MeridianScoringResultDTO scoringResultDTO = meridianHealthScoringService.score(windowSnapshot,
                userBaselineModel,
                ruleConfig,
                activityScene,
                windowSegment.getMetricSnapshotList().size());

        HealthColorStatusEnum previousColor = null;
        if (previousResult != null && previousResult.getHealthStateJudgeResponse() != null
                && previousResult.getHealthStateJudgeResponse().getHealthColorStatus() != null) {
            previousColor = HealthColorStatusEnum.valueOf(previousResult.getHealthStateJudgeResponse().getHealthColorStatus());
        }

        HealthColorDecisionResultDTO colorDecisionResultDTO = healthColorDecisionService.decide(request.getUserId(),
                scoringResultDTO,
                windowSnapshot,
                previousColor);

        HealthStateJudgeResponse response = new HealthStateJudgeResponse();
        response.setOverallScore(scoringResultDTO.getOverallScore());
        response.setLoadScore(scoringResultDTO.getLoadScore());
        response.setRecoveryHealthScore(scoringResultDTO.getRecoveryHealthScore());
        response.setOxygenScore(scoringResultDTO.getOxygenScore());
        response.setRhythmMatchScore(scoringResultDTO.getRhythmMatchScore());
        response.setBaselineDeviationScore(scoringResultDTO.getBaselineDeviationScore());
        response.setConfidenceScore(scoringResultDTO.getConfidenceScore());
        response.setHealthColorStatus(colorDecisionResultDTO.getHealthColorStatus().name());
        response.setMainDrivers(colorDecisionResultDTO.getMainDrivers());
        response.setColorChangeReason(colorDecisionResultDTO.getColorChangeReason());

        meridianCopywritingService.fillCopywriting(response, scoringResultDTO, colorDecisionResultDTO, ruleConfig);

        RealtimeJudgeResult realtimeJudgeResult = new RealtimeJudgeResult();
        realtimeJudgeResult.setUserId(request.getUserId());
        realtimeJudgeResult.setMeridianTimeSlot(meridianTimeSlot);
        realtimeJudgeResult.setResultTime(LocalDateTime.now());
        realtimeJudgeResult.setHealthStateJudgeResponse(response);
        inMemoryDataStore.saveRealtimeResult(request.getUserId(), realtimeJudgeResult);

        return response;
    }

    /**
     * 生成前端30分钟汇总状态。
     *
     * @param userId 用户标识
     * @param currentTime 当前时间
     * @return 30分钟汇总响应
     */
    public HealthStateJudgeResponse aggregateThirtyMinuteStatus(String userId, LocalDateTime currentTime) {
        List<RealtimeJudgeResult> historyList = inMemoryDataStore.getRealtimeResultHistory(userId).stream()
                .filter(item -> item.getHealthStateJudgeResponse() != null)
                .filter(item -> !item.getResultTime().isBefore(currentTime.minusMinutes(30)))
                .sorted(Comparator.comparing(RealtimeJudgeResult::getResultTime))
                .collect(Collectors.toList());
        if (historyList.isEmpty()) {
            return inMemoryDataStore.getRealtimeResult(userId) == null
                    ? null
                    : inMemoryDataStore.getRealtimeResult(userId).getHealthStateJudgeResponse();
        }

        HealthStateJudgeResponse aggregatedResponse = new HealthStateJudgeResponse();
        aggregatedResponse.setOverallScore(avgDouble(historyList.stream().map(item -> item.getHealthStateJudgeResponse().getOverallScore()).collect(Collectors.toList())));
        aggregatedResponse.setLoadScore(avgDouble(historyList.stream().map(item -> item.getHealthStateJudgeResponse().getLoadScore()).collect(Collectors.toList())));
        aggregatedResponse.setRecoveryHealthScore(avgDouble(historyList.stream().map(item -> item.getHealthStateJudgeResponse().getRecoveryHealthScore()).collect(Collectors.toList())));
        aggregatedResponse.setOxygenScore(avgDouble(historyList.stream().map(item -> item.getHealthStateJudgeResponse().getOxygenScore()).collect(Collectors.toList())));
        aggregatedResponse.setRhythmMatchScore(avgDouble(historyList.stream().map(item -> item.getHealthStateJudgeResponse().getRhythmMatchScore()).collect(Collectors.toList())));
        aggregatedResponse.setBaselineDeviationScore(avgDouble(historyList.stream().map(item -> item.getHealthStateJudgeResponse().getBaselineDeviationScore()).collect(Collectors.toList())));
        aggregatedResponse.setConfidenceScore(avgDouble(historyList.stream().map(item -> item.getHealthStateJudgeResponse().getConfidenceScore()).collect(Collectors.toList())));

        List<String> mainDrivers = historyList.get(historyList.size() - 1).getHealthStateJudgeResponse().getMainDrivers();
        aggregatedResponse.setMainDrivers(mainDrivers);
        aggregatedResponse.setColorChangeReason("前端显示为最近30分钟汇总状态");

        String colorStatus = resolveAggregatedColor(historyList);
        aggregatedResponse.setHealthColorStatus(colorStatus);

        HealthStateJudgeResponse lastResponse = historyList.get(historyList.size() - 1).getHealthStateJudgeResponse();
        aggregatedResponse.setMedicalSummary(lastResponse.getMedicalSummary());
        aggregatedResponse.setTcmMeridianSummary(lastResponse.getTcmMeridianSummary());
        aggregatedResponse.setActionAdvice(lastResponse.getActionAdvice());
        return aggregatedResponse;
    }

    private String resolveAggregatedColor(List<RealtimeJudgeResult> historyList) {
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

    private List<WearableMetricSnapshot> loadCrossDateCandidateList(String userId, LocalDateTime measureTime) {
        LocalDate currentDate = measureTime.toLocalDate();
        LocalDate previousDate = currentDate.minusDays(1);
        List<WearableMetricSnapshot> list = new ArrayList<>();
        list.addAll(inMemoryDataStore.getRawInputData(userId, previousDate));
        list.addAll(inMemoryDataStore.getRawInputData(userId, currentDate));
        return list;
    }

    private WearableMetricSnapshot convertToSnapshot(HealthStateJudgeRequest request) {
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
        snapshot.setStepCount(request.getStepCount());
        snapshot.setCalorieBurn(request.getCalorieBurn());
        snapshot.setEmotionState(request.getEmotionState());
        snapshot.setDeviceStressScore(request.getDeviceStressScore());
        snapshot.setDeviceFatigueScore(request.getDeviceFatigueScore());
        snapshot.setDeviceRecoveryScore(request.getDeviceRecoveryScore());
        snapshot.setMeasureTime(request.getMeasureTime());
        return snapshot;
    }

    private WearableMetricSnapshot aggregateWindowSnapshot(String userId,
                                                           List<WearableMetricSnapshot> snapshotList,
                                                           MeridianTimeSlotEnum meridianTimeSlot,
                                                           LocalDateTime measureTime) {
        WearableMetricSnapshot result = new WearableMetricSnapshot();
        result.setUserId(userId);
        result.setMeridianTimeSlot(meridianTimeSlot);
        result.setMeasureTime(measureTime);

        result.setHeartRate(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getHeartRate).collect(Collectors.toList())));
        result.setSpo2(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSpo2).collect(Collectors.toList())));
        result.setSd1(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSd1).collect(Collectors.toList())));
        result.setSd2(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSd2).collect(Collectors.toList())));
        result.setHf(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getHf).collect(Collectors.toList())));
        result.setLf(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getLf).collect(Collectors.toList())));
        result.setVlf(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getVlf).collect(Collectors.toList())));
        result.setSampleEntropy(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getSampleEntropy).collect(Collectors.toList())));
        result.setApproximateEntropy(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getApproximateEntropy).collect(Collectors.toList())));
        result.setDfaAlpha1(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getDfaAlpha1).collect(Collectors.toList())));
        result.setDfaAlpha2(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getDfaAlpha2).collect(Collectors.toList())));
        result.setRespiratoryRate(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getRespiratoryRate).collect(Collectors.toList())));
        result.setStepCount(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getStepCount).collect(Collectors.toList())));
        result.setCalorieBurn(avgDouble(snapshotList.stream().map(WearableMetricSnapshot::getCalorieBurn).collect(Collectors.toList())));
        result.setEmotionState(lastEmotion(snapshotList));
        result.setDeviceStressScore(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getDeviceStressScore).collect(Collectors.toList())));
        result.setDeviceFatigueScore(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getDeviceFatigueScore).collect(Collectors.toList())));
        result.setDeviceRecoveryScore(avgInteger(snapshotList.stream().map(WearableMetricSnapshot::getDeviceRecoveryScore).collect(Collectors.toList())));
        return result;
    }

    private Double avgDouble(List<Double> valueList) {
        List<Double> valid = valueList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (valid.isEmpty()) {
            return null;
        }
        return valid.stream().mapToDouble(Double::doubleValue).average().orElse(0D);
    }

    private Integer avgInteger(List<Integer> valueList) {
        List<Integer> valid = valueList.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (valid.isEmpty()) {
            return null;
        }
        return (int) Math.round(valid.stream().mapToInt(Integer::intValue).average().orElse(0D));
    }

    private String lastEmotion(List<WearableMetricSnapshot> snapshotList) {
        if (snapshotList.isEmpty()) {
            return null;
        }
        return snapshotList.get(snapshotList.size() - 1).getEmotionState();
    }
}
