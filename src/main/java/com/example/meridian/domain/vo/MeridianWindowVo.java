package com.example.meridian.domain.vo;
import lombok.Data;import java.time.LocalDateTime;
/** 时辰窗口 */
@Data public class MeridianWindowVo { private String meridianSlotCode; private LocalDateTime slotStartTime; private LocalDateTime slotEndTime; private LocalDateTime windowStartTime; private LocalDateTime windowEndTime; private long cumulativeMinutesFromSlotStart; private long windowMinutes; private boolean reachedMinWindow; private boolean slotEnded; }
