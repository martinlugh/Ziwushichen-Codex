# 子午流注时辰健康状态判断系统（无数据库版）

本项目是一个基于 **Spring Boot 3.x + Java 17** 的无数据库健康判断系统。
系统仅消费外部算法输出的现成指标，不在项目内重新计算 HRV/熵/频域/DFA/呼吸衍生指标。

## 1. 启动方式

### 1.1 环境要求
- JDK 17
- Maven 3.9+

### 1.2 启动命令
```bash
mvn spring-boot:run
```

如需关闭演示数据注入：
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--app.demo.enabled=false
```

## 2. 调用顺序建议

1. 先调用 `/api/device/data/vitals` 写入生理指标。
2. 再调用 `/api/device/data/activity` 写入活动指标。
3. 调用 `/api/meridian/realtime/current` 获取当前实时状态。
4. 调用 `/api/meridian/model-progress` 观察建模进度。
5. 需要时调用 `/api/meridian/baseline/rebuild` 强制重建基线。
6. 调用 `/api/meridian/summary/timeslot` 与 `/api/meridian/summary/daily` 观察结算结果。

## 3. 核心接口示例

统一返回结构：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T10:00:00",
  "data": {}
}
```

---

### 3.1 POST /api/device/data/vitals

请求：
```json
{
  "userId": "demo-user",
  "heartRate": 76,
  "spo2": 97,
  "sd1": 28,
  "sd2": 60,
  "hf": 280,
  "lf": 260,
  "vlf": 210,
  "sampleEntropy": 1.2,
  "approximateEntropy": 1.1,
  "dfaAlpha1": 1.0,
  "dfaAlpha2": 1.1,
  "respiratoryRate": 16,
  "emotionState": "平静",
  "deviceStressScore": 42,
  "deviceFatigueScore": 46,
  "deviceRecoveryScore": 74,
  "measureTime": "2026-04-19 10:00:00"
}
```

响应：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T10:00:01",
  "data": "生理数据接收成功"
}
```

---

### 3.2 POST /api/device/data/activity

请求：
```json
{
  "userId": "demo-user",
  "stepCount": 120,
  "calorieBurn": 10.5,
  "measureTime": "2026-04-19 10:00:00"
}
```

响应：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T10:00:02",
  "data": "活动数据接收成功"
}
```

---

### 3.3 GET /api/meridian/realtime/current?userId=demo-user

响应示例：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T10:01:00",
  "data": {
    "overallScore": 82.4,
    "loadScore": 80.0,
    "recoveryHealthScore": 84.0,
    "oxygenScore": 92.0,
    "rhythmMatchScore": 81.0,
    "baselineDeviationScore": 76.0,
    "confidenceScore": 83.0,
    "healthColorStatus": "HEALTHY",
    "mainDrivers": ["节律匹配良好", "恢复状态稳定"],
    "colorChangeReason": "评分稳定且风险占比较低，维持健康状态",
    "medicalSummary": "当前处于脾经主导时段...本结果仅用于健康状态管理，不用于疾病诊断。",
    "tcmMeridianSummary": "脾经主运化...本系统输出为调养建议，不提供疾病诊断结论。",
    "actionAdvice": ["建议工作间隙短时活动，保持节律性补水。"]
  }
}
```

---

### 3.4 GET /api/meridian/summary/timeslot?userId=demo-user&meridianTimeSlot=SI

响应示例：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T11:00:00",
  "data": {
    "userId": "demo-user",
    "meridianTimeSlot": "SI",
    "slotStartTime": "2026-04-19T09:00:00",
    "slotEndTime": "2026-04-19T11:00:00",
    "sampleCount": 18,
    "settlementSummary": "本时辰共采集18条数据...不用于疾病诊断。",
    "settlementTime": "2026-04-19T11:00:00"
  }
}
```

---

### 3.5 GET /api/meridian/summary/daily?userId=demo-user&date=2026-04-19

响应示例：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T22:00:00",
  "data": {
    "userId": "demo-user",
    "date": "2026-04-19",
    "sampleCount": 120,
    "avgOverallScore": 79.3,
    "dailyConclusion": "当日存在轻度波动，建议继续观察。",
    "keyObservations": ["部分时段出现恢复不足或负荷上升"]
  }
}
```

---

### 3.6 POST /api/meridian/baseline/rebuild

请求：
```json
{
  "userId": "demo-user",
  "meridianTimeSlot": "SI"
}
```

响应示例：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T10:05:00",
  "data": {
    "userId": "demo-user",
    "meridianTimeSlot": "SI",
    "baselineType": "PERSONAL_HEALTHY",
    "baselineBuildTime": "2026-04-19T10:05:00"
  }
}
```

---

### 3.7 GET /api/meridian/model-progress?userId=demo-user&meridianTimeSlot=SI

响应示例：
```json
{
  "code": "SUCCESS",
  "message": "处理成功",
  "timestamp": "2026-04-19T10:06:00",
  "data": {
    "userId": "demo-user",
    "meridianTimeSlot": "SI",
    "candidateCount": 24,
    "eligibleCount": 19,
    "currentType": "PERSONAL_HEALTHY",
    "progressRemark": "按近3天健康筛选结果构建完成",
    "updateTime": "2026-04-19T10:05:30"
  }
}
```

## 4. 如何观察基线构建

- 调用 `/api/meridian/baseline/rebuild` 后再调用 `/api/meridian/model-progress`。
- 系统也会在定时任务中自动触发基线构建。

## 5. 如何观察实时状态与时辰结算

- 使用 `/api/meridian/realtime/current` 查看实时状态。
- 每次时辰切换后，系统会在实时流程和定时任务中触发时辰结算。
- 使用 `/api/meridian/summary/timeslot` 查看结算结果。

## 6. 定时任务说明

系统内置三类内存版定时任务：
- 基线构建任务
- 实时计算任务
- 时辰结算任务

无需数据库即可运行。
