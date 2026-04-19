package com.example.meridian.domain.request;
import com.example.meridian.domain.dto.MeridianInputDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
/** 评估请求 */
@Data public class MeridianEvaluateRequest { @NotBlank private String userId; @Valid @NotNull private MeridianInputDto input; }
