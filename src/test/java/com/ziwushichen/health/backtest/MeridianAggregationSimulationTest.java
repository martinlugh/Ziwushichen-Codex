package com.ziwushichen.health.backtest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziwushichen.health.domain.entity.MeridianSettlementResult;
import com.ziwushichen.health.domain.request.api.DeviceActivityRequest;
import com.ziwushichen.health.domain.request.api.DeviceVitalsRequest;
import com.ziwushichen.health.domain.response.HealthStateJudgeResponse;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.service.DeviceDataIngestService;
import com.ziwushichen.health.service.MeridianOpenApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 30分钟汇总与时辰汇总模拟测试。
 */
@SpringBootTest(properties = {"app.demo.enabled=false"})
public class MeridianAggregationSimulationTest {

    @Autowired
    private DeviceDataIngestService deviceDataIngestService;

    @Autowired
    private MeridianOpenApiService meridianOpenApiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 模拟4个30分钟区间（共2小时）并验证时辰汇总。
     * 每5分钟输入一次数据。
     *
     * @throws Exception 异常
     */
    @Test
    public void simulateFourHalfHourAndTimeslotSummary() throws Exception {
        String userId = "agg-user";
        LocalDateTime startTime = LocalDateTime.of(2026, 4, 20, 9, 0, 0);
        List<HealthStateJudgeResponse> thirtyMinuteSummaryList = new ArrayList<>();

        for (int index = 0; index < 24; index++) {
            LocalDateTime measureTime = startTime.plusMinutes(index * 5L);
            DeviceVitalsRequest vitalsRequest = buildVitals(userId, measureTime, index);
            DeviceActivityRequest activityRequest = buildActivity(userId, measureTime, index);
            deviceDataIngestService.ingestVitals(vitalsRequest);
            deviceDataIngestService.ingestActivity(activityRequest);

            HealthStateJudgeResponse response = meridianOpenApiService.getRealtimeCurrent(userId);

            if ((index + 1) % 6 == 0) {
                thirtyMinuteSummaryList.add(response);
                System.out.println("30分钟汇总结果=" + objectMapper.writeValueAsString(response));
                Assertions.assertNotNull(response.getOverallScore());
                Assertions.assertTrue(response.getOverallScore() >= 0D && response.getOverallScore() <= 100D);
                Assertions.assertNotNull(response.getHealthColorStatus());
            }
        }

        Assertions.assertEquals(4, thirtyMinuteSummaryList.size());

        MeridianSettlementResult settlementResult = meridianOpenApiService.getTimeslotSummary(userId, MeridianTimeSlotEnum.SI);
        System.out.println("时辰汇总结果=" + objectMapper.writeValueAsString(settlementResult));
        Assertions.assertEquals(MeridianTimeSlotEnum.SI, settlementResult.getMeridianTimeSlot());
        Assertions.assertTrue(settlementResult.getSampleCount() >= 24);
        Assertions.assertTrue(settlementResult.getSettlementSummary().contains("前一时辰汇总状态="));
    }

    private DeviceVitalsRequest buildVitals(String userId, LocalDateTime measureTime, int index) {
        DeviceVitalsRequest request = new DeviceVitalsRequest();
        request.setUserId(userId);
        request.setHeartRate(75D + (index % 8));
        request.setSpo2(97D - (index % 3 == 0 ? 1D : 0D));
        request.setSd1(24D + (index % 4));
        request.setSd2(52D + (index % 6));
        request.setHf(250D + index * 3D);
        request.setLf(240D + index * 2D);
        request.setVlf(200D + index * 2D);
        request.setSampleEntropy(1.0D + (index % 3) * 0.05D);
        request.setApproximateEntropy(0.95D + (index % 3) * 0.05D);
        request.setDfaAlpha1(0.9D + (index % 4) * 0.05D);
        request.setDfaAlpha2(1.0D + (index % 4) * 0.04D);
        request.setRespiratoryRate(15D + (index % 5));
        request.setEmotionState(index % 7 == 0 ? "不愉悦" : (index % 2 == 0 ? "平静" : "愉悦"));
        request.setDeviceStressScore(40 + index % 20);
        request.setDeviceFatigueScore(42 + index % 18);
        request.setDeviceRecoveryScore(72 - index % 15);
        request.setMeasureTime(measureTime);
        return request;
    }

    private DeviceActivityRequest buildActivity(String userId, LocalDateTime measureTime, int index) {
        DeviceActivityRequest request = new DeviceActivityRequest();
        request.setUserId(userId);
        request.setStepCount(50 + index * 4);
        request.setCalorieBurn(6D + index * 0.3D);
        request.setMeasureTime(measureTime);
        return request;
    }
}
