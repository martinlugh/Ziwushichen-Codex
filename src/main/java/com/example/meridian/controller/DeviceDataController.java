package com.example.meridian.controller;

import com.example.meridian.common.response.ApiResponse;
import com.example.meridian.domain.request.ActivityUploadRequest;
import com.example.meridian.domain.request.VitalUploadRequest;
import com.example.meridian.service.DeviceDataIngestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/** 设备数据控制器 */
@RestController
@RequestMapping("/api/device/data")
@RequiredArgsConstructor
public class DeviceDataController {
    private final DeviceDataIngestService ingestService;
    @PostMapping("/vitals") public ApiResponse<String> vitals(@Valid @RequestBody VitalUploadRequest req){ ingestService.saveVitals(req); return ApiResponse.success("vitals数据已保存"); }
    @PostMapping("/activity") public ApiResponse<String> activity(@Valid @RequestBody ActivityUploadRequest req){ ingestService.saveActivity(req); return ApiResponse.success("activity数据已保存"); }
}
