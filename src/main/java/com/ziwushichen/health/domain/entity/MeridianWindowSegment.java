package com.ziwushichen.health.domain.entity;

import com.ziwushichen.health.enums.MeridianTimeSlotEnum;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 时辰窗口片段实体。
 */
@Data
public class MeridianWindowSegment {

    private String userId;

    private MeridianTimeSlotEnum meridianTimeSlot;

    private LocalDateTime slotStartTime;

    private LocalDateTime slotEndTime;

    private LocalDateTime windowStartTime;

    private LocalDateTime windowEndTime;

    private long effectiveMinutes;

    private boolean reachedMinimumWindow;

    private List<WearableMetricSnapshot> metricSnapshotList;
}
