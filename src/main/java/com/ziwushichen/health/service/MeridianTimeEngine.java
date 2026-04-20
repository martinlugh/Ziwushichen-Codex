package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.dto.MeridianTimeContextDTO;
import com.ziwushichen.health.domain.entity.MeridianWindowSegment;
import com.ziwushichen.health.domain.entity.WearableMetricSnapshot;
import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 时辰时间引擎。
 */
@Service
public class MeridianTimeEngine {

    /**
     * 最小判断窗口分钟数。
     */
    private static final long MINIMUM_JUDGE_WINDOW_MINUTES = 5L;

    /**
     * 滑动窗口分钟数。
     */
    private static final long SLIDING_WINDOW_MINUTES = 30L;

    /**
     * 判断当前时间所属时辰。
     *
     * @param currentTime 当前时间
     * @return 时辰枚举
     */
    public MeridianTimeSlotEnum resolveCurrentMeridianTimeSlot(LocalDateTime currentTime) {
        LocalTime localTime = currentTime.toLocalTime();
        for (MeridianTimeSlotEnum meridianTimeSlotEnum : MeridianTimeSlotEnum.values()) {
            if (meridianTimeSlotEnum.contains(localTime)) {
                return meridianTimeSlotEnum;
            }
        }
        throw new IllegalArgumentException("无法识别当前时辰");
    }

    /**
     * 计算时辰上下文。
     *
     * @param currentTime 当前时间
     * @param previousMeridianTimeSlot 上一个时辰
     * @return 上下文
     */
    public MeridianTimeContextDTO buildMeridianTimeContext(LocalDateTime currentTime,
                                                           MeridianTimeSlotEnum previousMeridianTimeSlot) {
        MeridianTimeSlotEnum currentMeridianTimeSlot = resolveCurrentMeridianTimeSlot(currentTime);
        LocalDateTime slotStartTime = calculateSlotStartTime(currentTime, currentMeridianTimeSlot);
        LocalDateTime slotEndTime = calculateSlotEndTime(currentTime, currentMeridianTimeSlot);
        long elapsedMinutesInSlot = calculateElapsedMinutesInSlot(currentTime, currentMeridianTimeSlot);

        MeridianTimeContextDTO contextDTO = new MeridianTimeContextDTO();
        contextDTO.setCurrentMeridianTimeSlot(currentMeridianTimeSlot);
        contextDTO.setPreviousMeridianTimeSlot(previousMeridianTimeSlot);
        contextDTO.setSlotStartTime(slotStartTime);
        contextDTO.setSlotEndTime(slotEndTime);
        contextDTO.setElapsedMinutesInSlot(elapsedMinutesInSlot);
        contextDTO.setSwitched(isSlotSwitched(previousMeridianTimeSlot, currentMeridianTimeSlot));
        contextDTO.setNeedSettlePreviousSlot(needSettlePreviousSlot(previousMeridianTimeSlot, currentMeridianTimeSlot));
        return contextDTO;
    }

    /**
     * 构建当前时辰有效窗口。
     *
     * @param userId 用户标识
     * @param currentTime 当前时间
     * @param metricSnapshotList 原始采样
     * @return 时辰窗口片段
     */
    public MeridianWindowSegment buildCurrentEffectiveWindow(String userId,
                                                             LocalDateTime currentTime,
                                                             List<WearableMetricSnapshot> metricSnapshotList) {
        MeridianTimeSlotEnum currentMeridianTimeSlot = resolveCurrentMeridianTimeSlot(currentTime);
        LocalDateTime slotStartTime = calculateSlotStartTime(currentTime, currentMeridianTimeSlot);
        LocalDateTime slotEndTime = calculateSlotEndTime(currentTime, currentMeridianTimeSlot);
        long elapsedMinutesInSlot = calculateElapsedMinutesInSlot(currentTime, currentMeridianTimeSlot);

        LocalDateTime windowStartTime;
        if (elapsedMinutesInSlot < SLIDING_WINDOW_MINUTES) {
            windowStartTime = slotStartTime;
        } else {
            windowStartTime = currentTime.minusMinutes(SLIDING_WINDOW_MINUTES);
            if (windowStartTime.isBefore(slotStartTime)) {
                windowStartTime = slotStartTime;
            }
        }

        LocalDateTime windowEndTime = currentTime;
        List<WearableMetricSnapshot> windowMetricSnapshotList = metricSnapshotList.stream()
                .filter(item -> item.getMeridianTimeSlot() == currentMeridianTimeSlot)
                .filter(item -> !item.getMeasureTime().isBefore(windowStartTime))
                .filter(item -> !item.getMeasureTime().isAfter(windowEndTime))
                .collect(Collectors.toList());

        MeridianWindowSegment meridianWindowSegment = new MeridianWindowSegment();
        meridianWindowSegment.setUserId(userId);
        meridianWindowSegment.setMeridianTimeSlot(currentMeridianTimeSlot);
        meridianWindowSegment.setSlotStartTime(slotStartTime);
        meridianWindowSegment.setSlotEndTime(slotEndTime);
        meridianWindowSegment.setWindowStartTime(windowStartTime);
        meridianWindowSegment.setWindowEndTime(windowEndTime);
        meridianWindowSegment.setEffectiveMinutes(elapsedMinutesInSlot);
        meridianWindowSegment.setReachedMinimumWindow(isReachedMinimumJudgeWindow(elapsedMinutesInSlot));
        meridianWindowSegment.setMetricSnapshotList(windowMetricSnapshotList);
        return meridianWindowSegment;
    }

    /**
     * 计算当前时辰累计有效分钟数。
     *
     * @param currentTime 当前时间
     * @param meridianTimeSlot 时辰
     * @return 有效分钟数
     */
    public long calculateElapsedMinutesInSlot(LocalDateTime currentTime, MeridianTimeSlotEnum meridianTimeSlot) {
        LocalDateTime slotStartTime = calculateSlotStartTime(currentTime, meridianTimeSlot);
        return Math.max(0L, Duration.between(slotStartTime, currentTime).toMinutes());
    }

    /**
     * 判断是否达到最小判断窗口。
     *
     * @param elapsedMinutesInSlot 累计分钟
     * @return 是否达到
     */
    public boolean isReachedMinimumJudgeWindow(long elapsedMinutesInSlot) {
        return elapsedMinutesInSlot >= MINIMUM_JUDGE_WINDOW_MINUTES;
    }

    /**
     * 判断是否发生时辰切换。
     *
     * @param previousMeridianTimeSlot 上一个时辰
     * @param currentMeridianTimeSlot 当前时辰
     * @return 是否切换
     */
    public boolean isSlotSwitched(MeridianTimeSlotEnum previousMeridianTimeSlot,
                                  MeridianTimeSlotEnum currentMeridianTimeSlot) {
        if (previousMeridianTimeSlot == null) {
            return false;
        }
        return previousMeridianTimeSlot != currentMeridianTimeSlot;
    }

    /**
     * 判断是否需要结算上一个时辰。
     *
     * @param previousMeridianTimeSlot 上一个时辰
     * @param currentMeridianTimeSlot 当前时辰
     * @return 是否需要结算
     */
    public boolean needSettlePreviousSlot(MeridianTimeSlotEnum previousMeridianTimeSlot,
                                          MeridianTimeSlotEnum currentMeridianTimeSlot) {
        return isSlotSwitched(previousMeridianTimeSlot, currentMeridianTimeSlot);
    }

    /**
     * 计算当前时辰起始时间。
     *
     * @param currentTime 当前时间
     * @param meridianTimeSlot 时辰
     * @return 起始时间
     */
    public LocalDateTime calculateSlotStartTime(LocalDateTime currentTime, MeridianTimeSlotEnum meridianTimeSlot) {
        LocalDate currentDate = currentTime.toLocalDate();
        LocalTime currentLocalTime = currentTime.toLocalTime();
        LocalTime startTime = meridianTimeSlot.getStartTime();
        LocalTime endTime = meridianTimeSlot.getEndTime();

        if (startTime.isAfter(endTime) && currentLocalTime.isBefore(endTime)) {
            return LocalDateTime.of(currentDate.minusDays(1), startTime);
        }
        return LocalDateTime.of(currentDate, startTime);
    }

    /**
     * 计算当前时辰结束时间。
     *
     * @param currentTime 当前时间
     * @param meridianTimeSlot 时辰
     * @return 结束时间
     */
    public LocalDateTime calculateSlotEndTime(LocalDateTime currentTime, MeridianTimeSlotEnum meridianTimeSlot) {
        LocalDateTime slotStartTime = calculateSlotStartTime(currentTime, meridianTimeSlot);
        LocalTime startTime = meridianTimeSlot.getStartTime();
        LocalTime endTime = meridianTimeSlot.getEndTime();
        if (startTime.isAfter(endTime)) {
            return LocalDateTime.of(slotStartTime.toLocalDate().plusDays(1), endTime);
        }
        return LocalDateTime.of(slotStartTime.toLocalDate(), endTime);
    }
}
