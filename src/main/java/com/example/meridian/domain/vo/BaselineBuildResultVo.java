package com.example.meridian.domain.vo;
import lombok.Data;
/** 基线构建结果 */
@Data public class BaselineBuildResultVo { private String userId; private String meridianSlotCode; private Integer candidateCount; private Integer healthyCount; private boolean generalReferenceUpdated; private boolean personalHealthyUpdated; private boolean personalStableUpdated; }
