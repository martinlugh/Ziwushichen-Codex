package com.ziwushichen.health.controller;

import com.ziwushichen.health.common.result.ApiResponse;
import com.ziwushichen.health.domain.entity.MeridianSettlementResult;
import com.ziwushichen.health.domain.entity.UserBaselineModel;
import com.ziwushichen.health.domain.request.api.BaselineRebuildRequest;
import com.ziwushichen.health.domain.request.api.DeviceActivityRequest;
import com.ziwushichen.health.domain.request.api.DeviceVitalsRequest;
import com.ziwushichen.health.domain.response.HealthStateJudgeResponse;
import com.ziwushichen.health.domain.response.api.DailySummaryResponse;
import com.ziwushichen.health.domain.response.api.ModelProgressResponse;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import com.ziwushichen.health.service.DeviceDataIngestService;
import com.ziwushichen.health.service.MeridianOpenApiService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 子午流注健康系统控制器。
 */
@RestController
public class MeridianHealthController {

    private final DeviceDataIngestService deviceDataIngestService;
    private final MeridianOpenApiService meridianOpenApiService;

    public MeridianHealthController(DeviceDataIngestService deviceDataIngestService,
                                    MeridianOpenApiService meridianOpenApiService) {
        this.deviceDataIngestService = deviceDataIngestService;
        this.meridianOpenApiService = meridianOpenApiService;
    }

    /**
     * 接收生理数据。
     */
    @PostMapping("/api/device/data/vitals")
    public ApiResponse<String> ingestVitals(@Valid @RequestBody DeviceVitalsRequest request) {
        deviceDataIngestService.ingestVitals(request);
        return ApiResponse.success("生理数据接收成功");
    }

    /**
     * 接收活动数据。
     */
    @PostMapping("/api/device/data/activity")
    public ApiResponse<String> ingestActivity(@Valid @RequestBody DeviceActivityRequest request) {
        deviceDataIngestService.ingestActivity(request);
        return ApiResponse.success("活动数据接收成功");
    }

    /**
     * 查询当前实时状态。
     */
    @GetMapping("/api/meridian/realtime/current")
    public ApiResponse<HealthStateJudgeResponse> getRealtimeCurrent(@RequestParam("userId") String userId) {
        return ApiResponse.success(meridianOpenApiService.getRealtimeCurrent(userId));
    }

    /**
     * 查询时辰结算。
     */
    @GetMapping("/api/meridian/summary/timeslot")
    public ApiResponse<MeridianSettlementResult> getTimeslotSummary(@RequestParam("userId") String userId,
                                                                    @RequestParam(value = "meridianTimeSlot", required = false) MeridianTimeSlotEnum meridianTimeSlot) {
        return ApiResponse.success(meridianOpenApiService.getTimeslotSummary(userId, meridianTimeSlot));
    }

    /**
     * 查询每日结算。
     */
    @GetMapping("/api/meridian/summary/daily")
    public ApiResponse<DailySummaryResponse> getDailySummary(@RequestParam("userId") String userId,
                                                             @RequestParam(value = "date", required = false)
                                                             @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return ApiResponse.success(meridianOpenApiService.getDailySummary(userId, date));
    }

    /**
     * 重建基线。
     */
    @PostMapping("/api/meridian/baseline/rebuild")
    public ApiResponse<UserBaselineModel> rebuildBaseline(@Valid @RequestBody BaselineRebuildRequest request) {
        return ApiResponse.success(meridianOpenApiService.rebuildBaseline(request.getUserId(), request.getMeridianTimeSlot()));
    }

    /**
     * 查询建模进度。
     */
    @GetMapping("/api/meridian/model-progress")
    public ApiResponse<ModelProgressResponse> getModelProgress(@RequestParam("userId") String userId,
                                                               @RequestParam(value = "meridianTimeSlot", required = false) MeridianTimeSlotEnum meridianTimeSlot) {
        return ApiResponse.success(meridianOpenApiService.getModelProgress(userId, meridianTimeSlot));
    }
}
