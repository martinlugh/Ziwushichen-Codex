package com.example.meridian.controller;

import com.example.meridian.common.response.ApiResponse;
import com.example.meridian.domain.entity.MeridianModelProgress;
import com.example.meridian.domain.entity.MeridianRuleConfig;
import com.example.meridian.domain.request.BaselineRebuildRequest;
import com.example.meridian.domain.response.MeridianEvaluateResponse;
import com.example.meridian.domain.vo.BaselineBuildResultVo;
import com.example.meridian.domain.vo.DailySummaryVo;
import com.example.meridian.domain.vo.TimeslotSummaryVo;
import com.example.meridian.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 子午流注控制器 */
@RestController
@RequestMapping("/api/meridian")
@RequiredArgsConstructor
public class MeridianController {
    private final MeridianQueryService queryService; private final MeridianBaselineBuildService baselineService; private final MeridianRuleConfigService ruleService; private final MeridianTimeEngine timeEngine;
    @GetMapping("/realtime/current") public ApiResponse<MeridianEvaluateResponse> current(@RequestParam String userId){ return ApiResponse.success(queryService.getCurrentRealtime(userId)); }
    @GetMapping("/summary/timeslot") public ApiResponse<List<TimeslotSummaryVo>> timeslot(@RequestParam String userId,@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date,@RequestParam(required=false) String slotCode){ return ApiResponse.success(queryService.getTimeslotSummary(userId,date,slotCode)); }
    @GetMapping("/summary/daily") public ApiResponse<DailySummaryVo> daily(@RequestParam String userId,@RequestParam @DateTimeFormat(pattern="yyyy-MM-dd") LocalDate date){ return ApiResponse.success(queryService.getDailySummary(userId,date)); }
    @PostMapping("/baseline/rebuild") public ApiResponse<BaselineBuildResultVo> rebuild(@Valid @RequestBody BaselineRebuildRequest req){ LocalDateTime t=req.getRebuildTime()==null?LocalDateTime.now():req.getRebuildTime(); String slot=timeEngine.resolveCurrentSlot(t).getCode(); return ApiResponse.success(baselineService.build(req.getUserId(),slot,t)); }
    @GetMapping("/model-progress") public ApiResponse<MeridianModelProgress> progress(@RequestParam String userId){ return ApiResponse.success(queryService.getModelProgress(userId)); }
    @GetMapping("/rule-config/list") public ApiResponse<List<MeridianRuleConfig>> rules(){ return ApiResponse.success(ruleService.listEnabled()); }
}
