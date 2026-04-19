package com.example.meridian.domain.request;
import jakarta.validation.constraints.NotBlank;import lombok.Data;import java.time.LocalDateTime;
/** 基线重建请求 */
@Data public class BaselineRebuildRequest { @NotBlank private String userId; private LocalDateTime rebuildTime; }
