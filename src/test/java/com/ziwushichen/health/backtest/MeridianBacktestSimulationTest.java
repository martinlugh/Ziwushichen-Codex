package com.ziwushichen.health.backtest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziwushichen.health.domain.entity.MeridianSettlementResult;
import com.ziwushichen.health.domain.request.api.BaselineRebuildRequest;
import com.ziwushichen.health.domain.request.api.DeviceActivityRequest;
import com.ziwushichen.health.domain.request.api.DeviceVitalsRequest;
import com.ziwushichen.health.domain.response.HealthStateJudgeResponse;
import com.ziwushichen.health.domain.response.api.DailySummaryResponse;
import com.ziwushichen.health.domain.response.api.ModelProgressResponse;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.service.DeviceDataIngestService;
import com.ziwushichen.health.service.MeridianOpenApiService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 回测模拟测试。
 */
@SpringBootTest(properties = {"app.demo.enabled=false"})
public class MeridianBacktestSimulationTest {

    @Autowired
    private DeviceDataIngestService deviceDataIngestService;

    @Autowired
    private MeridianOpenApiService meridianOpenApiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 场景一：模拟输入并输出实时结果。
     *
     * @throws Exception 异常
     */
    @Test
    public void backtestRealtimeFlow() throws Exception {
        BacktestFilePayload payload = readBacktestPayload();
        BacktestExpectedPayload expectedPayload = readExpectedPayload();
        List<HealthStateJudgeResponse> responseList = new ArrayList<>();

        for (int index = 0; index < payload.getSamples().size(); index++) {
            BacktestSample sample = payload.getSamples().get(index);
            DeviceVitalsRequest vitalsRequest = new DeviceVitalsRequest();
            vitalsRequest.setUserId(payload.getUserId());
            vitalsRequest.setHeartRate(sample.getHeartRate());
            vitalsRequest.setSpo2(sample.getSpo2());
            vitalsRequest.setSd1(sample.getSd1());
            vitalsRequest.setSd2(sample.getSd2());
            vitalsRequest.setHf(sample.getHf());
            vitalsRequest.setLf(sample.getLf());
            vitalsRequest.setVlf(sample.getVlf());
            vitalsRequest.setSampleEntropy(sample.getSampleEntropy());
            vitalsRequest.setApproximateEntropy(sample.getApproximateEntropy());
            vitalsRequest.setDfaAlpha1(sample.getDfaAlpha1());
            vitalsRequest.setDfaAlpha2(sample.getDfaAlpha2());
            vitalsRequest.setRespiratoryRate(sample.getRespiratoryRate());
            vitalsRequest.setEmotionState(sample.getEmotionState());
            vitalsRequest.setDeviceStressScore(sample.getDeviceStressScore());
            vitalsRequest.setDeviceFatigueScore(sample.getDeviceFatigueScore());
            vitalsRequest.setDeviceRecoveryScore(sample.getDeviceRecoveryScore());
            vitalsRequest.setMeasureTime(sample.getMeasureTime());
            deviceDataIngestService.ingestVitals(vitalsRequest);

            DeviceActivityRequest activityRequest = new DeviceActivityRequest();
            activityRequest.setUserId(payload.getUserId());
            activityRequest.setStepCount(sample.getStepCount());
            activityRequest.setCalorieBurn(sample.getCalorieBurn());
            activityRequest.setMeasureTime(sample.getMeasureTime());
            deviceDataIngestService.ingestActivity(activityRequest);

            HealthStateJudgeResponse response = meridianOpenApiService.getRealtimeCurrent(payload.getUserId());
            responseList.add(response);
            String json = objectMapper.writeValueAsString(response);
            System.out.println("回测实时结果=" + json);

            BacktestExpectedResult expectedResult = expectedPayload.getResults().get(index);
            double roundedOverallScore = Math.round(response.getOverallScore() * 100D) / 100D;
            Assertions.assertEquals(expectedResult.getOverallScore(), roundedOverallScore);
            Assertions.assertEquals(expectedResult.getHealthColorStatus(), response.getHealthColorStatus());
        }

        Assertions.assertFalse(responseList.isEmpty());
        HealthStateJudgeResponse last = responseList.get(responseList.size() - 1);
        Assertions.assertNotNull(last.getOverallScore());
        Assertions.assertTrue(last.getOverallScore() >= 0D && last.getOverallScore() <= 100D);
        Assertions.assertNotNull(last.getHealthColorStatus());
    }

    /**
     * 场景二：验证时辰结算、每日总结、模型进度与基线重建。
     *
     * @throws Exception 异常
     */
    @Test
    public void backtestSummaryAndBaselineFlow() throws Exception {
        BacktestFilePayload payload = readBacktestPayload();

        ModelProgressResponse modelProgressResponse = meridianOpenApiService.getModelProgress(payload.getUserId(), MeridianTimeSlotEnum.SI);
        System.out.println("建模进度=" + objectMapper.writeValueAsString(modelProgressResponse));
        Assertions.assertEquals(payload.getUserId(), modelProgressResponse.getUserId());

        MeridianSettlementResult settlementResult = meridianOpenApiService.getTimeslotSummary(payload.getUserId(), MeridianTimeSlotEnum.SI);
        System.out.println("时辰结算=" + objectMapper.writeValueAsString(settlementResult));
        Assertions.assertNotNull(settlementResult.getMeridianTimeSlot());

        DailySummaryResponse dailySummaryResponse = meridianOpenApiService.getDailySummary(payload.getUserId(), LocalDate.of(2026, 4, 20));
        System.out.println("每日总结=" + objectMapper.writeValueAsString(dailySummaryResponse));
        Assertions.assertEquals(payload.getUserId(), dailySummaryResponse.getUserId());

        BaselineRebuildRequest baselineRebuildRequest = new BaselineRebuildRequest();
        baselineRebuildRequest.setUserId(payload.getUserId());
        baselineRebuildRequest.setMeridianTimeSlot(MeridianTimeSlotEnum.SI);
        Assertions.assertNotNull(meridianOpenApiService.rebuildBaseline(baselineRebuildRequest.getUserId(), baselineRebuildRequest.getMeridianTimeSlot()));
    }

    private BacktestFilePayload readBacktestPayload() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("test/backtest-samples.json");
        try (InputStream inputStream = classPathResource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<BacktestFilePayload>() {
            });
        }
    }

    private BacktestExpectedPayload readExpectedPayload() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("test/backtest-expected-results.json");
        try (InputStream inputStream = classPathResource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<BacktestExpectedPayload>() {
            });
        }
    }

    /**
     * 回测文件数据对象。
     */
    public static class BacktestFilePayload {
        private String userId;
        private List<BacktestSample> samples;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<BacktestSample> getSamples() {
            return samples;
        }

        public void setSamples(List<BacktestSample> samples) {
            this.samples = samples;
        }
    }

    /**
     * 回测采样对象。
     */
    public static class BacktestSample {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime measureTime;
        private Double heartRate;
        private Double spo2;
        private Double sd1;
        private Double sd2;
        private Double hf;
        private Double lf;
        private Double vlf;
        private Double sampleEntropy;
        private Double approximateEntropy;
        private Double dfaAlpha1;
        private Double dfaAlpha2;
        private Double respiratoryRate;
        private String emotionState;
        private Integer deviceStressScore;
        private Integer deviceFatigueScore;
        private Integer deviceRecoveryScore;
        private Integer stepCount;
        private Double calorieBurn;

        public LocalDateTime getMeasureTime() {
            return measureTime;
        }

        public void setMeasureTime(LocalDateTime measureTime) {
            this.measureTime = measureTime;
        }

        public Double getHeartRate() {
            return heartRate;
        }

        public void setHeartRate(Double heartRate) {
            this.heartRate = heartRate;
        }

        public Double getSpo2() {
            return spo2;
        }

        public void setSpo2(Double spo2) {
            this.spo2 = spo2;
        }

        public Double getSd1() {
            return sd1;
        }

        public void setSd1(Double sd1) {
            this.sd1 = sd1;
        }

        public Double getSd2() {
            return sd2;
        }

        public void setSd2(Double sd2) {
            this.sd2 = sd2;
        }

        public Double getHf() {
            return hf;
        }

        public void setHf(Double hf) {
            this.hf = hf;
        }

        public Double getLf() {
            return lf;
        }

        public void setLf(Double lf) {
            this.lf = lf;
        }

        public Double getVlf() {
            return vlf;
        }

        public void setVlf(Double vlf) {
            this.vlf = vlf;
        }

        public Double getSampleEntropy() {
            return sampleEntropy;
        }

        public void setSampleEntropy(Double sampleEntropy) {
            this.sampleEntropy = sampleEntropy;
        }

        public Double getApproximateEntropy() {
            return approximateEntropy;
        }

        public void setApproximateEntropy(Double approximateEntropy) {
            this.approximateEntropy = approximateEntropy;
        }

        public Double getDfaAlpha1() {
            return dfaAlpha1;
        }

        public void setDfaAlpha1(Double dfaAlpha1) {
            this.dfaAlpha1 = dfaAlpha1;
        }

        public Double getDfaAlpha2() {
            return dfaAlpha2;
        }

        public void setDfaAlpha2(Double dfaAlpha2) {
            this.dfaAlpha2 = dfaAlpha2;
        }

        public Double getRespiratoryRate() {
            return respiratoryRate;
        }

        public void setRespiratoryRate(Double respiratoryRate) {
            this.respiratoryRate = respiratoryRate;
        }

        public String getEmotionState() {
            return emotionState;
        }

        public void setEmotionState(String emotionState) {
            this.emotionState = emotionState;
        }

        public Integer getDeviceStressScore() {
            return deviceStressScore;
        }

        public void setDeviceStressScore(Integer deviceStressScore) {
            this.deviceStressScore = deviceStressScore;
        }

        public Integer getDeviceFatigueScore() {
            return deviceFatigueScore;
        }

        public void setDeviceFatigueScore(Integer deviceFatigueScore) {
            this.deviceFatigueScore = deviceFatigueScore;
        }

        public Integer getDeviceRecoveryScore() {
            return deviceRecoveryScore;
        }

        public void setDeviceRecoveryScore(Integer deviceRecoveryScore) {
            this.deviceRecoveryScore = deviceRecoveryScore;
        }

        public Integer getStepCount() {
            return stepCount;
        }

        public void setStepCount(Integer stepCount) {
            this.stepCount = stepCount;
        }

        public Double getCalorieBurn() {
            return calorieBurn;
        }

        public void setCalorieBurn(Double calorieBurn) {
            this.calorieBurn = calorieBurn;
        }
    }

    /**
     * 期望结果文件对象。
     */
    public static class BacktestExpectedPayload {
        private String userId;
        private List<BacktestExpectedResult> results;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public List<BacktestExpectedResult> getResults() {
            return results;
        }

        public void setResults(List<BacktestExpectedResult> results) {
            this.results = results;
        }
    }

    /**
     * 单次期望结果对象。
     */
    public static class BacktestExpectedResult {
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime measureTime;
        private Double overallScore;
        private String healthColorStatus;

        public LocalDateTime getMeasureTime() {
            return measureTime;
        }

        public void setMeasureTime(LocalDateTime measureTime) {
            this.measureTime = measureTime;
        }

        public Double getOverallScore() {
            return overallScore;
        }

        public void setOverallScore(Double overallScore) {
            this.overallScore = overallScore;
        }

        public String getHealthColorStatus() {
            return healthColorStatus;
        }

        public void setHealthColorStatus(String healthColorStatus) {
            this.healthColorStatus = healthColorStatus;
        }
    }
}
