package com.ziwushichen.health.backtest;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.core.type.TypeReference;
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
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于测试文件的时辰回测。
 */
@SpringBootTest(properties = {"app.demo.enabled=false"})
public class MeridianTimeslotBacktestFromFileTest {

    @Autowired
    private DeviceDataIngestService deviceDataIngestService;

    @Autowired
    private MeridianOpenApiService meridianOpenApiService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 使用测试文件模拟一个完整时辰并输出汇总结果。
     *
     * @throws Exception 异常
     */
    @Test
    public void backtestOneTimeslotFromFile() throws Exception {
        TimeslotBacktestPayload payload = readPayload();
        List<HealthStateJudgeResponse> halfHourSummaryList = new ArrayList<>();

        for (int index = 0; index < payload.getSamples().size(); index++) {
            TimeslotSample sample = payload.getSamples().get(index);
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

            HealthStateJudgeResponse realtimeResponse = meridianOpenApiService.getRealtimeCurrent(payload.getUserId());
            if ((index + 1) % 6 == 0) {
                halfHourSummaryList.add(realtimeResponse);
                System.out.println("30分钟汇总输出=" + objectMapper.writeValueAsString(realtimeResponse));
            }
        }

        MeridianSettlementResult settlementResult = meridianOpenApiService.getTimeslotSummary(payload.getUserId(), MeridianTimeSlotEnum.valueOf(payload.getMeridianTimeSlot()));
        System.out.println("时辰汇总输出=" + objectMapper.writeValueAsString(settlementResult));

        Assertions.assertEquals(4, halfHourSummaryList.size());
        Assertions.assertEquals(MeridianTimeSlotEnum.valueOf(payload.getMeridianTimeSlot()), settlementResult.getMeridianTimeSlot());
        Assertions.assertTrue(settlementResult.getSettlementSummary().contains("前一时辰汇总状态="));
    }

    private TimeslotBacktestPayload readPayload() throws Exception {
        ClassPathResource classPathResource = new ClassPathResource("test/timeslot-backtest-samples.json");
        try (InputStream inputStream = classPathResource.getInputStream()) {
            return objectMapper.readValue(inputStream, new TypeReference<TimeslotBacktestPayload>() {
            });
        }
    }

    /**
     * 时辰回测输入对象。
     */
    public static class TimeslotBacktestPayload {
        private String userId;
        private String meridianTimeSlot;
        private List<TimeslotSample> samples;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getMeridianTimeSlot() {
            return meridianTimeSlot;
        }

        public void setMeridianTimeSlot(String meridianTimeSlot) {
            this.meridianTimeSlot = meridianTimeSlot;
        }

        public List<TimeslotSample> getSamples() {
            return samples;
        }

        public void setSamples(List<TimeslotSample> samples) {
            this.samples = samples;
        }
    }

    /**
     * 时辰回测单条样本。
     */
    public static class TimeslotSample {
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
        private Integer stepCount;
        private Double calorieBurn;
        private String emotionState;
        private Integer deviceStressScore;
        private Integer deviceFatigueScore;
        private Integer deviceRecoveryScore;

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
    }
}
