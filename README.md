# Meridian Health System

## 项目说明
基于穿戴设备数据与子午流注时辰的健康管理后端，技术栈：Java 17 + Spring Boot 3 + MyBatis-Plus + MySQL 8。

## 数据库初始化
1. 先执行第1轮建表SQL（包含10张业务表）。
2. 执行示例初始化：
```bash
mysql -uroot -proot meridian_health < db/init-data.sql
```

## 启动步骤
1. 修改 `src/main/resources/application.yml` 中数据库连接。
2. 启动：
```bash
mvn clean spring-boot:run
```

## 接口列表
- POST `/api/device/data/vitals`
- POST `/api/device/data/activity`
- GET `/api/meridian/realtime/current`
- GET `/api/meridian/summary/timeslot`
- GET `/api/meridian/summary/daily`
- POST `/api/meridian/baseline/rebuild`
- GET `/api/meridian/model-progress`
- GET `/api/meridian/rule-config/list`

## 测试流程
1. 先调用上传接口写入同一时辰数据。
2. 调用实时接口查看当前结果。
3. 调用基线重建接口。
4. 查看时辰/每日汇总与模型进度。

## 示例请求
### vitals
```json
{
  "userId":"U1001",
  "heartRate":73,
  "spo2":97.5,
  "rrIntervals":[810,805,820,815],
  "emotionState":"CALM",
  "deviceStressScore":45,
  "deviceFatigueScore":39,
  "deviceRecoveryScore":64,
  "measureTime":"2026-04-18T09:30:00"
}
```
