package com.ziwushichen.health.init;

import com.ziwushichen.health.domain.request.api.DeviceActivityRequest;
import com.ziwushichen.health.domain.request.api.DeviceVitalsRequest;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.service.DeviceDataIngestService;
import com.ziwushichen.health.service.MeridianBaselineBuildService;
import com.ziwushichen.health.service.MeridianOpenApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 演示数据初始化组件。
 */
@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final DeviceDataIngestService deviceDataIngestService;
    private final MeridianBaselineBuildService meridianBaselineBuildService;
    private final MeridianOpenApiService meridianOpenApiService;

    @Value("${app.demo.enabled:true}")
    private boolean demoEnabled;

    public DemoDataInitializer(DeviceDataIngestService deviceDataIngestService,
                               MeridianBaselineBuildService meridianBaselineBuildService,
                               MeridianOpenApiService meridianOpenApiService) {
        this.deviceDataIngestService = deviceDataIngestService;
        this.meridianBaselineBuildService = meridianBaselineBuildService;
        this.meridianOpenApiService = meridianOpenApiService;
    }

    @Override
    public void run(String... args) {
        if (!demoEnabled) {
            return;
        }

        String userId = "demo-user";
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 12; i++) {
            LocalDateTime time = now.minusMinutes(10L * i);

            DeviceVitalsRequest vitalsRequest = new DeviceVitalsRequest();
            vitalsRequest.setUserId(userId);
            vitalsRequest.setHeartRate(72D + i % 5);
            vitalsRequest.setSpo2(97D - i % 2);
            vitalsRequest.setSd1(25D + i % 4);
            vitalsRequest.setSd2(55D + i % 6);
            vitalsRequest.setHf(260D + i * 3);
            vitalsRequest.setLf(240D + i * 3);
            vitalsRequest.setVlf(200D + i * 2);
            vitalsRequest.setSampleEntropy(1.2D);
            vitalsRequest.setApproximateEntropy(1.1D);
            vitalsRequest.setDfaAlpha1(1.0D);
            vitalsRequest.setDfaAlpha2(1.1D);
            vitalsRequest.setRespiratoryRate(16D + i % 2);
            vitalsRequest.setEmotionState(i >= 8 ? "不愉悦" : "平静");
            vitalsRequest.setDeviceStressScore(38 + i % 8);
            vitalsRequest.setDeviceFatigueScore(40 + i % 6);
            vitalsRequest.setDeviceRecoveryScore(72 - i % 6);
            vitalsRequest.setMeasureTime(time);
            deviceDataIngestService.ingestVitals(vitalsRequest);

            DeviceActivityRequest activityRequest = new DeviceActivityRequest();
            activityRequest.setUserId(userId);
            activityRequest.setStepCount(45 + i * 5);
            activityRequest.setCalorieBurn(6D + i * 0.4D);
            activityRequest.setMeasureTime(time);
            deviceDataIngestService.ingestActivity(activityRequest);
        }

        MeridianTimeSlotEnum currentSlot = MeridianTimeSlotEnum.valueOf(now.getHour() >= 23 || now.getHour() < 1 ? "ZI"
                : now.getHour() < 3 ? "CHOU"
                : now.getHour() < 5 ? "YIN"
                : now.getHour() < 7 ? "MAO"
                : now.getHour() < 9 ? "CHEN"
                : now.getHour() < 11 ? "SI"
                : now.getHour() < 13 ? "WU"
                : now.getHour() < 15 ? "WEI"
                : now.getHour() < 17 ? "SHEN"
                : now.getHour() < 19 ? "YOU"
                : now.getHour() < 21 ? "XU" : "HAI");
        meridianBaselineBuildService.buildBaseline(userId, currentSlot, now);
        meridianOpenApiService.getRealtimeCurrent(userId);
    }
}
