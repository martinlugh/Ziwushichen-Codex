package com.ziwushichen.health.service;

import com.ziwushichen.health.domain.dto.HealthColorDecisionResultDTO;
import com.ziwushichen.health.domain.dto.MeridianScoringResultDTO;
import com.ziwushichen.health.domain.response.HealthStateJudgeResponse;
import com.ziwushichen.health.rule.MeridianRuleConfig;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 文案生成服务。
 */
@Service
public class MeridianCopywritingService {

    /**
     * 生成三段式文案。
     *
     * @param response 响应对象
     * @param scoringResultDTO 评分结果
     * @param colorDecisionResultDTO 颜色结果
     * @param ruleConfig 规则配置
     */
    public void fillCopywriting(HealthStateJudgeResponse response,
                                MeridianScoringResultDTO scoringResultDTO,
                                HealthColorDecisionResultDTO colorDecisionResultDTO,
                                MeridianRuleConfig ruleConfig) {
        String medicalSummary = ruleConfig.getMedicalTemplate()
                .replace("{meridianName}", ruleConfig.getMeridianName())
                + " 当前综合评分为" + format(scoringResultDTO.getOverallScore())
                + "分，恢复评分" + format(scoringResultDTO.getRecoveryHealthScore())
                + "分，氧合评分" + format(scoringResultDTO.getOxygenScore()) + "分。";

        String tcmSummary = ruleConfig.getTcmTemplate()
                .replace("{meridianName}", ruleConfig.getMeridianName())
                + " 当前关注维度为" + ruleConfig.getFocusDimension()
                + "，状态判定为" + colorDecisionResultDTO.getHealthColorStatus().name() + "。";

        List<String> actionAdvice = new ArrayList<>();
        actionAdvice.add(ruleConfig.getAdviceTemplate());
        if (scoringResultDTO.getLoadScore() < 60D) {
            actionAdvice.add("建议在接下来30分钟降低活动强度，优先稳定心率与呼吸节律。");
        }
        if (scoringResultDTO.getRecoveryHealthScore() < 60D) {
            actionAdvice.add("建议增加放松恢复行为，例如短时闭目休息或舒缓拉伸。");
        }
        if (scoringResultDTO.getOxygenScore() < 60D) {
            actionAdvice.add("建议关注呼吸深度与节律，保证环境通风并避免久坐。");
        }

        response.setMedicalSummary(medicalSummary + " 本结果仅用于健康状态管理，不用于疾病诊断。");
        response.setTcmMeridianSummary(tcmSummary + " 本系统输出为调养建议，不提供疾病诊断结论。");
        response.setActionAdvice(actionAdvice);
    }

    private String format(Double score) {
        if (score == null) {
            return "0";
        }
        return String.format("%.1f", score);
    }
}
